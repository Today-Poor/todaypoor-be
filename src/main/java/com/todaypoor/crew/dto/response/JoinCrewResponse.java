package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record JoinCrewResponse(

        UUID crewId,
        String crewName,
        CrewRole role,
        Integer currentMemberCount,
        Integer maxMemberCount,
        LocalDateTime joinedAt

) {

    public static JoinCrewResponse of(Crew crew, CrewMember crewMember, int currentMemberCount) {

        return new JoinCrewResponse(

                crew.getId(),
                crew.getName(),
                crewMember.getRole(),
                currentMemberCount,
                crew.getMaxMemberCount(),
                crewMember.getJoinedAt()

        );
    }
}
