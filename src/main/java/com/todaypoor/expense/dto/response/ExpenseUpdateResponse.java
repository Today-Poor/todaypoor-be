package com.todaypoor.expense.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpenseUpdateResponse {

    private UUID expenseId;
    private ExpenseCategory category;
    private Integer amount;
    private String merchant;
    private String memo;
    private ExpenseVisibility visibility;
    private LocalDateTime spentAt;
    private LocalDateTime updatedAt;

    public static ExpenseUpdateResponse from(Expense expense) {
        return new ExpenseUpdateResponse(
                expense.getId(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getMerchant(),
                expense.getMemo(),
                expense.getVisibility(),
                expense.getSpentAt(),
                expense.getUpdatedAt()
        );
    }
}