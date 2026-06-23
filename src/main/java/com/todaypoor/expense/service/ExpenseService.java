package com.todaypoor.expense.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.todaypoor.ai.client.ClaudeOcrParserClient;
import com.todaypoor.ai.client.GoogleVisionClient;
import com.todaypoor.ai.entity.OcrResult;
import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.*;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import lombok.RequiredArgsConstructor;

import com.todaypoor.expense.dto.request.ExpenseSaveRequest;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.ai.repository.OcrResultRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final OcrResultRepository ocrResultRepository;
    private final GoogleVisionClient googleVisionClient;
    private final ClaudeOcrParserClient claudeOcrParserClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExpenseSaveResponse saveExpenses(UUID userId, UUID crewId, ExpenseSaveRequest request) {
        UUID ocrResultId;
        try {
            ocrResultId = UUID.fromString(request.getOcrResultId());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        ocrResultRepository.findById(ocrResultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_RESULT_NOT_FOUND));

        List<Expense> expensesToSave = request.getExpenses().stream()
                .map(detail -> createExpenseEntity(userId, crewId, ocrResultId, detail))
                .collect(Collectors.toList());

        List<Expense> savedExpenses = expenseRepository.saveAll(expensesToSave);

        return ExpenseSaveResponse.of(request.getOcrResultId(), savedExpenses);
    }

    private Expense createExpenseEntity(UUID userId, UUID crewId, UUID ocrResultId, ExpenseSaveRequest.ExpenseDetail detail) {
        return Expense.create(
                userId,
                crewId,
                ocrResultId,
                detail.getCategory(),
                detail.getAmount(),
                detail.getMerchant(),
                detail.getMemo(),
                detail.getVisibility(),
                detail.getSpentAt()
        );
    }

    @Transactional
    public ExpenseUpdateResponse updateExpense(UUID userId, UUID crewId, UUID expenseId, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getCrewId().equals(crewId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        expense.update(
                request.getCategory(),
                request.getAmount(),
                request.getMerchant(),
                request.getMemo(),
                request.getVisibility(),
                request.getSpentAt()
        );

        return ExpenseUpdateResponse.from(expense);
    }

    @Transactional
    public void deleteExpense(UUID userId, UUID crewId, UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getCrewId().equals(crewId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        expense.softDelete();
        expenseRepository.save(expense);
    }

    public MemberExpenseListResponse getMemberExpenses(
            UUID requestUserId, UUID crewId, UUID targetUserId, LocalDate date, Pageable pageable) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Page<Expense> expensePage = expenseRepository.findByCrewIdAndUserIdAndSpentAtBetweenOrderBySpentAtDesc(
                crewId, targetUserId, startOfDay, endOfDay, pageable
        );

        long totalAmount = expenseRepository.sumAmountByCrewIdAndUserIdAndDate(
                crewId, targetUserId, startOfDay, endOfDay).orElse(0L);

        // TODO: 크루/유저 도메인 연동 후 실제 데이터로 교체
        String crewName = "거지방 1조";
        String nickname = "철수";
        String profileImageUrl = "https://image-url.com/1.png";

        return MemberExpenseListResponse.of(
                crewId, crewName, targetUserId, nickname, profileImageUrl,
                date, totalAmount, expensePage.getContent(), requestUserId
        );
    }

    public ExpenseDetailResponse getExpenseDetail(UUID requestUserId, UUID crewId, UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getCrewId().equals(crewId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // TODO: 유저 도메인 연동 후 실제 데이터로 교체
        ExpenseDetailResponse.UserInfo userInfo = ExpenseDetailResponse.UserInfo.of(
                expense.getUserId(), "철수", "https://image-url.com/1.png"
        );

        // TODO: OCR 도메인 연동 후 실제 데이터로 교체
        ExpenseDetailResponse.OcrResultInfo ocrInfo = null;
        if (expense.getOcrResultId() != null) {
            ocrInfo = ExpenseDetailResponse.OcrResultInfo.of(
                    expense.getOcrResultId(),
                    "맘스터치 대구점 6,500원...",
                    List.of()
            );
        }

        return ExpenseDetailResponse.of(expense, requestUserId, userInfo, ocrInfo);
    }

    @Transactional
    public OcrAnalyzeResponse analyzeReceipt(UUID crewId, UUID userId, MultipartFile image) {

        // 1. Google Vision API로 이미지에서 텍스트 추출
        String rawText = googleVisionClient.extractText(image);

        // 2. Claude AI로 텍스트에서 소비 내역 구조화 파싱
        List<OcrAnalyzeResponse.ParsedExpense> parsedList = claudeOcrParserClient.parseExpenses(rawText);

        if (parsedList.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_PARSED_EXPENSES);
        }

        // 3. OCR 분석 결과를 DB에 저장
        String parsedDataJson = serializeParsedExpenses(parsedList);
        OcrResult ocrResult = OcrResult.create(rawText, parsedDataJson);
        ocrResultRepository.save(ocrResult);

        return OcrAnalyzeResponse.of(ocrResult.getId(), rawText, parsedList);
    }

    private String serializeParsedExpenses(List<OcrAnalyzeResponse.ParsedExpense> parsedList) {
        try {
            return objectMapper.writeValueAsString(parsedList);
        } catch (JacksonException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}