package com.todaypoor.crew.dto.crew.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;
import java.util.Objects;

public record CrewMainResponse(

        UUID crewId,
        String crewName,
        Integer currentMemberCount,
        Integer maxMemberCount,
        List<MemberSummary> members

) {

    public static CrewMainResponse of(
            Crew crew,
            Integer currentMemberCount,
            List<MemberSummary> members
    ) {

        return new CrewMainResponse(
                crew.getId(),
                crew.getName(),
                currentMemberCount,
                crew.getMaxMemberCount(),
                members
        );
    }

    public record MemberSummary(

            UUID userId,
            String nickname,
            String profileImageUrl,
            CrewRole role,
            LatestExpense latestExpense
    ) {
        public static MemberSummary of(CrewMember crewMember, LatestExpense latestExpense, String nickname) {
            return new MemberSummary(
                    crewMember.getUserId(),
                    nickname,
                    null,
                    crewMember.getRole(),
                    latestExpense
            );
        }
    }

    public record LatestExpense(
            UUID expenseId,
            ExpenseCategory category,
            Integer amount,
            ExpenseVisibility visibility,
            LocalDateTime spentAt
    ) {
        public static LatestExpense from(Expense expense, UUID requestUserId) {
            if (expense == null) {
                return null;
            }

            boolean isOwner = Objects.equals(expense.getUserId(), requestUserId);
            ExpenseVisibility visibility = expense.getVisibility();

            return new LatestExpense(
                    expense.getId(),
                    isOwner ? expense.getCategory() : visibility.maskCategory(expense.getCategory()),
                    isOwner ? expense.getAmount()   : visibility.maskAmount(expense.getAmount()),
                    visibility,
                    expense.getSpentAt()
            );
        }
    }

}
