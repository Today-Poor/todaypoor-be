package com.todaypoor.expense.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpenseSaveResponse {

    private String ocrResultId;
    private int savedCount;
    private List<SavedExpenseDetail> expenses;

    public static ExpenseSaveResponse of(String ocrResultId, List<Expense> savedExpenses) {
        List<SavedExpenseDetail> detailList = savedExpenses.stream()
                .map(SavedExpenseDetail::from)
                .collect(Collectors.toList());

        return new ExpenseSaveResponse(ocrResultId, detailList.size(), detailList);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE) // 추가됨!
    public static class SavedExpenseDetail {
        private UUID expenseId;
        private ExpenseCategory category;
        private Integer amount;
        private String merchant;
        private String memo;
        private ExpenseVisibility visibility;
        private LocalDateTime spentAt;
        private LocalDateTime createdAt;

        public static SavedExpenseDetail from(Expense expense) {
            return new SavedExpenseDetail(
                    expense.getId(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getMerchant(),
                    expense.getMemo(),
                    expense.getVisibility(),
                    expense.getSpentAt(),
                    expense.getCreatedAt()
            );
        }
    }
}