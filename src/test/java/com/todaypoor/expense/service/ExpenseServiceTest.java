package com.todaypoor.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import tools.jackson.databind.ObjectMapper;
import com.todaypoor.ai.client.ClaudeOcrParserClient;
import com.todaypoor.ai.client.GoogleVisionClient;
import com.todaypoor.ai.entity.OcrResult;
import com.todaypoor.ai.repository.OcrResultRepository;
import com.todaypoor.expense.dto.response.OcrAnalyzeResponse;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;
import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private OcrResultRepository ocrResultRepository;

    @Mock
    private GoogleVisionClient googleVisionClient;

    @Mock
    private ClaudeOcrParserClient claudeOcrParserClient;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("OCR 영수증 분석 시 Google Vision → Claude → DB 저장 순서로 호출되고 결과를 반환한다.")
    void analyzeReceipt_success() throws Exception {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "receipt.jpg", "image/jpeg", "dummy image content".getBytes()
        );

        String rawText = "맘스터치 대구점 6,500원 2026.05.20 14:30\n스타벅스 5,800원 2026.05.20 13:10";
        List<OcrAnalyzeResponse.ParsedExpense> parsedExpenses = List.of(
                OcrAnalyzeResponse.ParsedExpense.of("temp-1", ExpenseCategory.FOOD, 6500, "맘스터치 대구점", LocalDateTime.of(2026, 5, 20, 14, 30)),
                OcrAnalyzeResponse.ParsedExpense.of("temp-2", ExpenseCategory.CAFE, 5800, "스타벅스", LocalDateTime.of(2026, 5, 20, 13, 10))
        );
        OcrResult mockResult = OcrResult.create(rawText, "[{\"category\":\"FOOD\"}]");

        given(googleVisionClient.extractText(mockImage)).willReturn(rawText);
        given(claudeOcrParserClient.parseExpenses(rawText)).willReturn(parsedExpenses);
        given(objectMapper.writeValueAsString(parsedExpenses)).willReturn("[{\"category\":\"FOOD\"}]");
        given(ocrResultRepository.save(any(OcrResult.class))).willReturn(mockResult);

        // when
        OcrAnalyzeResponse response = expenseService.analyzeReceipt(crewId, userId, mockImage);

        // then
        assertThat(response.getRawText()).isEqualTo(rawText);
        assertThat(response.getParsedExpenses()).hasSize(2);
        assertThat(response.getParsedExpenses().get(0).getMerchant()).isEqualTo("맘스터치 대구점");
        verify(googleVisionClient).extractText(mockImage);
        verify(claudeOcrParserClient).parseExpenses(rawText);
        verify(ocrResultRepository).save(any(OcrResult.class));
    }

    @Test
    @DisplayName("Google Vision API 실패 시 OCR_FAILED 예외가 발생한다.")
    void analyzeReceipt_throwsOcrFailed_whenVisionClientFails() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "receipt.jpg", "image/jpeg", "dummy".getBytes()
        );

        given(googleVisionClient.extractText(any())).willThrow(new BusinessException(ErrorCode.OCR_FAILED));

        // when & then
        assertThatThrownBy(() -> expenseService.analyzeReceipt(crewId, userId, mockImage))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OCR_FAILED);
    }

    @Test
    @DisplayName("Claude AI 파싱 실패 시 AI_PARSING_FAILED 예외가 발생한다.")
    void analyzeReceipt_throwsAiParsingFailed_whenClaudeClientFails() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "receipt.jpg", "image/jpeg", "dummy".getBytes()
        );

        given(googleVisionClient.extractText(any())).willReturn("영수증 원문");
        given(claudeOcrParserClient.parseExpenses(anyString()))
                .willThrow(new BusinessException(ErrorCode.AI_PARSING_FAILED));

        // when & then
        assertThatThrownBy(() -> expenseService.analyzeReceipt(crewId, userId, mockImage))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_PARSING_FAILED);
    }

    @Test
    @DisplayName("AI 파싱 결과가 0건이면 EMPTY_PARSED_EXPENSES 예외가 발생한다.")
    void analyzeReceipt_throwsEmptyParsedExpenses_whenParsedListIsEmpty() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "receipt.jpg", "image/jpeg", "dummy".getBytes()
        );

        given(googleVisionClient.extractText(any())).willReturn("읽을 수 없는 텍스트");
        given(claudeOcrParserClient.parseExpenses(anyString())).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> expenseService.analyzeReceipt(crewId, userId, mockImage))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPTY_PARSED_EXPENSES);
    }

    @Test
    @DisplayName("결제 내역 상세 조회 시, 해당 크루의 내역이 아니면 FORBIDDEN 예외가 발생한다.")
    void getExpenseDetail_throwsForbidden() {
        // given
        UUID requestCrewId = UUID.randomUUID();
        UUID otherCrewId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        Expense expense = Expense.create(
                UUID.randomUUID(), otherCrewId, UUID.randomUUID(), ExpenseCategory.FOOD,
                5000, "맥도날드", "점심", ExpenseVisibility.PUBLIC, LocalDateTime.now()
        );

        given(expenseRepository.findById(expenseId)).willReturn(Optional.of(expense));

        // when & then
        assertThatThrownBy(() -> expenseService.getExpenseDetail(UUID.randomUUID(), requestCrewId, expenseId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}
