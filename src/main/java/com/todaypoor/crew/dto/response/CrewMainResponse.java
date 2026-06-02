package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

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
        public static MemberSummary of(CrewMember crewMember, LatestExpense latestExpense) {

            // TODO: User 도메인 연동 후 nickname, profileImageUrl 채울 예정
            return new MemberSummary(

                    crewMember.getUserId(),
                    null,
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
        public static LatestExpense from(Expense expense) {
            if (expense == null) {
                return null;
            }

            return new LatestExpense(

                    expense.getId(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getVisibility(),
                    expense.getSpentAt()
            );
        }
    }

}
