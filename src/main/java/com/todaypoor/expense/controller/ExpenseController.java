package com.todaypoor.expense.controller;

import java.util.UUID;

import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.ExpenseUpdateResponse;
import com.todaypoor.global.response.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.todaypoor.expense.dto.request.ExpenseSaveRequest;
import com.todaypoor.expense.dto.response.ExpenseSaveResponse;
import com.todaypoor.expense.service.ExpenseService;

@RestController
@RequestMapping("/api/crews/{crewId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseSaveResponse>> saveExpenses(
            @PathVariable UUID crewId,
            @Valid @RequestBody ExpenseSaveRequest request
    ) {

        UUID userId = UUID.randomUUID(); //현재는 랜덤값, 나중에 수정예정

        ExpenseSaveResponse response = expenseService.saveExpenses(userId, crewId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseUpdateResponse>> updateExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        UUID userId = UUID.randomUUID(); //임시로 사용 중

        ExpenseUpdateResponse response = expenseService.updateExpense(userId, crewId, expenseId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable UUID crewId,
            @PathVariable UUID expenseId
    ) {
        UUID userId = UUID.randomUUID(); // 임시로 적어둠.

        expenseService.deleteExpense(userId, crewId, expenseId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}