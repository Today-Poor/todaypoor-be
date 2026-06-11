package com.todaypoor.crew.dto.crewMember.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record CrewMemberListResponse(

        UUID crewId,
        String crewName,
        List<CrewMemberList> members
) {
    public static CrewMemberListResponse of(Crew crew, List<CrewMemberList> members) {
        return new CrewMemberListResponse(
                crew.getId(),
                crew.getName(),
                members
        );
    }

    public record CrewMemberList(
            UUID userId,
            String nickname,
            String profileImageUrl,
            CrewRole role,
            LocalDateTime joinedAt
    ) {
        public static CrewMemberList from(CrewMember crewMember) {

            // TODO: User 도메인 연동 후 nickname, profileImageUrl 채울 예정
            return new CrewMemberList(

                    crewMember.getUserId(),
                    null,
                    null,
                    crewMember.getRole(),
                    crewMember.getJoinedAt()
            );
        }
    }

}
