package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;

public record UpdateCrewResponse(
        UUID crewId,
        String name,
        String description,
        Integer maxMemberCount,
        Integer currentMemberCount,
        AiMode aiMode,
        LocalDateTime updatedAt
) {
    public static UpdateCrewResponse of(Crew crew, Integer currentMemberCount) {

        return new UpdateCrewResponse(

                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getMaxMemberCount(),
                currentMemberCount,
                crew.getAiMode(),
                crew.getUpdatedAt()
        );
    }
}
