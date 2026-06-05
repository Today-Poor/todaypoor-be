package com.todaypoor.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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

    @Test
    @DisplayName("OCR 영수증 분석 가짜(Mock) 로직이 정상적으로 응답을 반환한다.")
    void analyzeReceipt_success() {
        // given (준비)
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "receipt.jpg", "image/jpeg", "dummy image content".getBytes()
        );

        OcrResult mockResult = OcrResult.create("원문", "파싱데이터");
        given(ocrResultRepository.save(any(OcrResult.class))).willReturn(mockResult);

        // when (실행)
        OcrAnalyzeResponse response = expenseService.analyzeReceipt(crewId, userId, mockImage);

        // then (검증)
        assertThat(response.getRawText()).contains("맘스터치"); // 우리가 넣은 가짜 데이터가 잘 오는지 확인!
        assertThat(response.getParsedExpenses()).hasSize(2); // 데이터 2개가 잘 파싱됐는지 확인!
        verify(ocrResultRepository).save(any(OcrResult.class)); // DB 저장이 1번이라도 호출됐는지 확인!
    }

    @Test
    @DisplayName("결제 내역 상세 조회 시, 해당 크루의 내역이 아니면 FORBIDDEN 예외가 발생한다.")
    void getExpenseDetail_throwsForbidden() {
        // given (준비)
        UUID requestCrewId = UUID.randomUUID();
        UUID otherCrewId = UUID.randomUUID(); // 다른 크루방 ID
        UUID expenseId = UUID.randomUUID();

        Expense expense = Expense.create(
                UUID.randomUUID(), otherCrewId, UUID.randomUUID(), ExpenseCategory.FOOD,
                5000, "맥도날드", "점심", ExpenseVisibility.PUBLIC, LocalDateTime.now()
        );

        given(expenseRepository.findById(expenseId)).willReturn(Optional.of(expense));

        // when & then (실행 및 예외 검증)
        assertThatThrownBy(() -> expenseService.getExpenseDetail(requestCrewId, expenseId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}