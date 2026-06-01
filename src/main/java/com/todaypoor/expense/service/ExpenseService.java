package com.todaypoor.expense.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.ExpenseDetailResponse;
import com.todaypoor.expense.dto.response.ExpenseUpdateResponse;
import com.todaypoor.expense.dto.response.MemberExpenseListResponse;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.todaypoor.expense.dto.request.ExpenseSaveRequest;
import com.todaypoor.expense.dto.response.ExpenseSaveResponse;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.repository.ExpenseRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseSaveResponse saveExpenses(UUID userId, UUID crewId, ExpenseSaveRequest request) {
        UUID ocrResultId = UUID.fromString(request.getOcrResultId());

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

        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        expenseRepository.delete(expense);
    }

    public MemberExpenseListResponse getMemberExpenses(UUID crewId, UUID userId, LocalDate date, Pageable pageable) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Page<Expense> expensePage = expenseRepository.findByCrewIdAndUserIdAndSpentAtBetweenOrderBySpentAtDesc(
                crewId, userId, startOfDay, endOfDay, pageable
        );

        long totalAmount = expenseRepository.sumAmountByCrewIdAndUserIdAndDate(
                crewId, userId, startOfDay, endOfDay).orElse(0L);

        String crewName = "거지방 1조";
        String nickname = "철수";
        String profileImageUrl = "https://image-url.com/1.png";

        return MemberExpenseListResponse.of(
                crewId, crewName, userId, nickname, profileImageUrl,
                date, totalAmount, expensePage.getContent()
        );
    }

    public ExpenseDetailResponse getExpenseDetail(UUID crewId, UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (!expense.getCrewId().equals(crewId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        //타 도메인 데이터 (유저 정보, OCR 결과) 임시 하드코딩 - 나중에 연동 해줘야함.
        ExpenseDetailResponse.UserInfo userInfo = ExpenseDetailResponse.UserInfo.of(
                expense.getUserId(), "철수", "https://image-url.com/1.png"
        );

        //여기도 하드코딩 해놨어 여원아... 이거 맞지? 첨해봐서 몰루?
        ExpenseDetailResponse.OcrResultInfo ocrInfo = null;
        if (expense.getOcrResultId() != null) {
            ocrInfo = ExpenseDetailResponse.OcrResultInfo.of(
                    expense.getOcrResultId(),
                    "맘스터치 대구점 6,500원...",
                    List.of()
            );
        }

        String emoji = "🍔"; // 나중에 카테고리별 분기 처리(Enum 내부 메서드 활용 등) 필요, 일단 하드코딩했다잉

        return ExpenseDetailResponse.of(expense, userInfo, ocrInfo, emoji);
    }
}