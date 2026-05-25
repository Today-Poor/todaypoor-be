package com.todaypoor.crew.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;

public record RegenerateInviteCodeResponse(

        UUID crewId,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt

) {

    public static RegenerateInviteCodeResponse from(Crew crew) {

        return new RegenerateInviteCodeResponse(

                crew.getId(),
                crew.getInviteCode(),
                crew.getInviteCodeExpiresAt()
        );
    }
}
