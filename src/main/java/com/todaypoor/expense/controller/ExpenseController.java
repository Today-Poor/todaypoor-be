package com.todaypoor.expense.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.*;
import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.security.CustomUserDetails;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;

import com.todaypoor.expense.dto.request.ExpenseSaveRequest;
import com.todaypoor.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "소비 지출 (Expense)", description = "크루 내 소비 지출 내역 관리 및 영수증 OCR 분석 API")
@RestController
@RequestMapping("/api/crews/{crewId}")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "소비 지출 내역 등록", description = "크루 내에 새로운 소비 지출 내역을 등록합니다.")
    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseSaveResponse>> saveExpenses(
            @PathVariable UUID crewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseSaveRequest request
    ) {
        ExpenseSaveResponse response = expenseService.saveExpenses(userDetails.getUserId(), crewId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "소비 지출 내역 수정", description = "등록된 소비 지출 내역의 금액, 내용, 카테고리 등을 수정합니다.")
    @PatchMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseUpdateResponse>> updateExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        ExpenseUpdateResponse response = expenseService.updateExpense(userDetails.getUserId(), crewId, expenseId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "소비 지출 내역 삭제", description = "등록된 소비 지출 내역을 삭제(Soft Delete)합니다.")
    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        expenseService.deleteExpense(userDetails.getUserId(), crewId, expenseId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "멤버별 소비 지출 목록 조회", description = "특정 크루 멤버의 특정 날짜 지출 내역 목록을 조회합니다. 페이지네이션을 지원합니다.")
    @GetMapping("/members/{userId}/expenses")
    public ResponseEntity<ApiResponse<MemberExpenseListResponse>> getMemberExpenses(
            @PathVariable UUID crewId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        MemberExpenseListResponse response = expenseService.getMemberExpenses(
                userDetails.getUserId(), crewId, userId, date, pageRequest);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "소비 지출 상세 조회", description = "지출 내역의 상세 정보를 조회합니다.")
    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDetailResponse>> getExpenseDetail(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExpenseDetailResponse response = expenseService.getExpenseDetail(userDetails.getUserId(), crewId, expenseId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "영수증 이미지 분석 (OCR)", description = "영수증 이미지를 업로드하여 OCR 분석을 통해 지출 금액과 카테고리를 자동으로 추출합니다.")
    @PostMapping(value = "/expenses/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OcrAnalyzeResponse>> analyzeReceipt(
            @PathVariable UUID crewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("image") MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }

        OcrAnalyzeResponse response = expenseService.analyzeReceipt(crewId, userDetails.getUserId(), image);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
