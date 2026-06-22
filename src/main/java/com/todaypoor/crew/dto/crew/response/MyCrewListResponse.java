package com.todaypoor.crew.dto.crew.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;

public record MyCrewListResponse(

        List<MyCrewSummary> crews
) {

    public static MyCrewListResponse of(List<MyCrewSummary> crews) {
        return new MyCrewListResponse(crews);
    }

    public record MyCrewSummary(

            UUID crewId,
            String name,
            String description,
            AiMode aiMode,
            CrewRole role,
            Integer currentMemberCount,
            Integer maxMemberCount,
            LocalDateTime createdAt
    ) {

        public static MyCrewSummary of(Crew crew, CrewMember crewMember, Integer currentMemberCount) {

            return new MyCrewSummary(

                    crew.getId(),
                    crew.getName(),
                    crew.getDescription(),
                    crew.getAiMode(),
                    crewMember.getRole(),
                    currentMemberCount,
                    crew.getMaxMemberCount(),
                    crew.getCreatedAt()

            );
        }
    }

}
