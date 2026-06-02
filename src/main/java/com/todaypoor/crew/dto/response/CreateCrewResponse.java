package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;

public record CreateCrewResponse(
        UUID crewId,
        String name,
        String description,
        Integer maxMemberCount,
        Integer currentMemberCount,
        AiMode aiMode,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt,
        Owner owner,
        LocalDateTime createdAt
) {

    // TODO: Owner.of 추가 예정
    public record Owner(UUID userId, String nickname, String profileImageUrl) {}

    public static CreateCrewResponse of(Crew crew, Owner owner, Integer currentMemberCount) {
        return new CreateCrewResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getMaxMemberCount(),
                currentMemberCount,
                crew.getAiMode(),
                crew.getInviteCode(),
                crew.getInviteCodeExpiresAt(),
                owner,
                crew.getCreatedAt()
        );
    }
}
