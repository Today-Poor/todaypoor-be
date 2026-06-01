package com.todaypoor.expense.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.todaypoor.expense.dto.request.ExpenseUpdateRequest;
import com.todaypoor.expense.dto.response.ExpenseUpdateResponse;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
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
}