package com.todaypoor.crew.dto.crew.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todaypoor.crew.entity.Crew;

public record InviteCodeResponse(

        UUID crewId,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt

) {

    public static InviteCodeResponse from(Crew crew) {

        return new InviteCodeResponse(

                crew.getId(),
                crew.getInviteCode(),
                crew.getInviteCodeExpiresAt()
        );
    }
}
