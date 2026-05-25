package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record JoinCrewResponse(

        UUID crewMemberId,
        UUID userId,
        UUID crewId,
        CrewRole role,
        LocalDateTime joinedAt

) {

    public static JoinCrewResponse from(CrewMember crewMember) {

        return new JoinCrewResponse(

                crewMember.getId(),
                crewMember.getUserId(),
                crewMember.getCrewId(),
                crewMember.getRole(),
                crewMember.getJoinedAt()
        );
    }
}
