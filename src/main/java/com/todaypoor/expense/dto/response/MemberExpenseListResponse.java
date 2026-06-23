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
import java.util.Objects;
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
            UUID crewId, String crewName, UUID targetUserId, String nickname, String profileImageUrl,
            LocalDate date, long totalAmount, List<Expense> expenseList, UUID requestUserId) {

        UserInfo userInfo = UserInfo.of(targetUserId, nickname, profileImageUrl);

        List<ExpenseSummary> summaries = expenseList.stream()
                .map(expense -> ExpenseSummary.from(expense, requestUserId))
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

        public static ExpenseSummary from(Expense expense, UUID requestUserId) {
            boolean isOwner = Objects.equals(expense.getUserId(), requestUserId);
            ExpenseVisibility visibility = expense.getVisibility();

            return new ExpenseSummary(
                    expense.getId(),
                    isOwner ? expense.getCategory() : visibility.maskCategory(expense.getCategory()),
                    isOwner ? expense.getAmount()   : visibility.maskAmount(expense.getAmount()),
                    isOwner ? expense.getMerchant() : visibility.maskMerchant(expense.getMerchant()),
                    isOwner ? expense.getMemo()     : visibility.maskMemo(expense.getMemo()),
                    visibility,
                    expense.getSpentAt()
            );
        }
    }
}