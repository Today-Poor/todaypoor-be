package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;

public record CreateCrewResponse(
        UUID crewId,
        String name,
        String description,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt,
        AiMode aiMode,
        UUID ownerId,
        LocalDateTime createdAt
) {

    public static CreateCrewResponse from(Crew crew) {
        return new CreateCrewResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getInviteCode(),
                crew.getInviteCodeExpiresAt(),
                crew.getAiMode(),
                crew.getOwnerId(),
                crew.getCreatedAt()
        );
    }
}
