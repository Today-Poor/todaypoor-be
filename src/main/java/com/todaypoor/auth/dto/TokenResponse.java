package com.todaypoor.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "인증 토큰 응답 DTO")
@Getter
public class TokenResponse {

    @Schema(description = "Access Token (만료 1시간, API 호출 인가용)")
    private final String accessToken;

    @Schema(description = "Refresh Token (만료 14일, 토큰 갱신용)")
    private final String refreshToken;

    @Builder
    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
