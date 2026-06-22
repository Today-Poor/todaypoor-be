package com.todaypoor.crew.dto.crewMember.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record CrewMemberDetailResponse(

        UUID crewId,
        UUID userId,
        String nickname,
        String profileImageUrl,
        CrewRole role,
        LocalDateTime joinedAt
) {

    public static CrewMemberDetailResponse from(CrewMember crewMember) {

        // TODO: User 도메인 연동 후 nickname, profileImageUrl 채울 예정
        return new CrewMemberDetailResponse(

                crewMember.getCrewId(),
                crewMember.getUserId(),
                null,
                null,
                crewMember.getRole(),
                crewMember.getJoinedAt()
        );
    }

}
