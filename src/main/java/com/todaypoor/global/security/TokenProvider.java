package com.todaypoor.global.security;

import java.util.UUID;

public interface TokenProvider {
    /**
     * Access Token을 생성합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @return 생성된 Access Token
     */
    String createAccessToken(UUID userId);

    /**
     * Refresh Token을 생성합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @return 생성된 Refresh Token
     */
    String createRefreshToken(UUID userId);

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효한 경우 true, 그렇지 않은 경우 false
     */
    boolean validateToken(String token);

    /**
     * 토큰에서 유저 고유 식별자 (userId)를 안전하게 추출합니다.
     *
     * @param token JWT 토큰
     * @return 유저 고유 식별자 (UUID)
     */
    UUID extractUserId(String token);
}
