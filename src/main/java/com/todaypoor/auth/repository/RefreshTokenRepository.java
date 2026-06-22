package com.todaypoor.auth.repository;

import java.util.UUID;

public interface RefreshTokenRepository {
    /**
     * Refresh Token을 저장합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @param refreshToken 발급된 Refresh Token
     * @param expirationTimeMs 만료 시간 (밀리초)
     */
    void save(UUID userId, String refreshToken, long expirationTimeMs);

    /**
     * 유저 식별자에 해당하는 Refresh Token을 조회합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @return 저장된 Refresh Token (존재하지 않으면 null)
     */
    String findByUserId(UUID userId);

    /**
     * 유저 식별자에 해당하는 Refresh Token을 삭제합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     */
    void deleteByUserId(UUID userId);
}
