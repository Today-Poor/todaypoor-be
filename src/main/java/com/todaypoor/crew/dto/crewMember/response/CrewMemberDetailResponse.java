package com.todaypoor.crew.dto.crewMember.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record CrewMemberDetailResponse(

        UUID crewId,
        UUID userId,
        String nickname,
        CrewRole role,
        LocalDateTime joinedAt
) {

    public static CrewMemberDetailResponse from(CrewMember crewMember, String nickname) {
        return new CrewMemberDetailResponse(
                crewMember.getCrewId(),
                crewMember.getUserId(),
                nickname,
                crewMember.getRole(),
                crewMember.getJoinedAt()
        );
    }

}
