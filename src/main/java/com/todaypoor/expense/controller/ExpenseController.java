package com.todaypoor.expense.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.*;
import com.todaypoor.global.response.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;

import com.todaypoor.expense.dto.request.ExpenseSaveRequest;
import com.todaypoor.expense.service.ExpenseService;

@RestController
@RequestMapping("/api/crews/{crewId}")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseSaveResponse>> saveExpenses(
            @PathVariable UUID crewId,
            @Valid @RequestBody ExpenseSaveRequest request
    ) {
        UUID userId = UUID.randomUUID();

        ExpenseSaveResponse response = expenseService.saveExpenses(userId, crewId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseUpdateResponse>> updateExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        UUID userId = UUID.randomUUID();

        ExpenseUpdateResponse response = expenseService.updateExpense(userId, crewId, expenseId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId
    ) {
        UUID userId = UUID.randomUUID();

        expenseService.deleteExpense(userId, crewId, expenseId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/members/{userId}/expenses")
    public ResponseEntity<ApiResponse<MemberExpenseListResponse>> getMemberExpenses(
            @PathVariable UUID crewId,
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        MemberExpenseListResponse response = expenseService.getMemberExpenses(crewId, userId, date, pageRequest);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDetailResponse>> getExpenseDetail(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId
    ) {
        ExpenseDetailResponse response = expenseService.getExpenseDetail(crewId, expenseId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/expenses/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OcrAnalyzeResponse>> analyzeReceipt(
            @PathVariable UUID crewId,
            @RequestPart("image") MultipartFile image // 키값은 api명세서에 image라고 적어둠.
    ) {
        UUID userId = UUID.randomUUID();

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }

        OcrAnalyzeResponse response = expenseService.analyzeReceipt(crewId, userId, image);

        return ResponseEntity.ok(ApiResponse.success(response));
    }



}