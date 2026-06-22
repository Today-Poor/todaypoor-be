package com.todaypoor.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserMeResponse {

    private final UUID userId;
    private final String nickname;

    @Builder
    public UserMeResponse(UUID userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
