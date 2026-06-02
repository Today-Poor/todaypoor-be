package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;

public record CrewDetailResponse(

        UUID crewId,
        String name,
        String description,
        AiMode aiMode,
        Integer currentMemberCount,
        Integer maxMemberCount,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt,
        Owner owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record Owner(UUID userId, String nickname, String profileImageUrl) {}

    public static CrewDetailResponse of(Crew crew, Owner owner, Integer currentMemberCount) {

        return new CrewDetailResponse(

                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getAiMode(),
                currentMemberCount,
                crew.getMaxMemberCount(),
                crew.getInviteCode(),
                crew.getInviteCodeExpiresAt(),
                owner,
                crew.getCreatedAt(),
                crew.getUpdatedAt()
        );
    }

}
