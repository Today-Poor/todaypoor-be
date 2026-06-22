package com.todaypoor.expense.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberExpenseListResponse {

    private UUID crewId;
    private String crewName;
    private UserInfo user;
    private LocalDate date;
    private long totalAmount;
    private List<ExpenseSummary> expenses;

    public static MemberExpenseListResponse of(
            UUID crewId, String crewName, UUID userId, String nickname, String profileImageUrl,
            LocalDate date, long totalAmount, List<Expense> expenseList) {

        UserInfo userInfo = UserInfo.of(userId, nickname, profileImageUrl);

        List<ExpenseSummary> summaries = expenseList.stream()
                .map(ExpenseSummary::from)
                .toList();

        return new MemberExpenseListResponse(crewId, crewName, userInfo, date, totalAmount, summaries);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserInfo {
        private UUID userId;
        private String nickname;
        private String profileImageUrl;

        public static UserInfo of(UUID userId, String nickname, String profileImageUrl) {
            return new UserInfo(userId, nickname, profileImageUrl);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ExpenseSummary {
        private UUID expenseId;
        private ExpenseCategory category;
        private Integer amount;
        private String merchant;
        private String memo;
        private ExpenseVisibility visibility;
        private LocalDateTime spentAt;

        public static ExpenseSummary from(Expense expense) {
            return new ExpenseSummary(
                    expense.getId(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getMerchant(),
                    expense.getMemo(),
                    expense.getVisibility(),
                    expense.getSpentAt()
            );
        }
    }
}