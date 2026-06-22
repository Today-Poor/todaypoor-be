package com.todaypoor.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "your-super-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha-256";
    private final long accessTokenExpiration = 3600000; // 1 hour
    private final long refreshTokenExpiration = 1209600000; // 14 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessTokenExpiration, refreshTokenExpiration);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성 성공 및 userId 추출 검증")
    void createAccessTokenAndExtractUserId() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        // then
        assertThat(accessToken).isNotNull();
        
        UUID extractedUserId = jwtTokenProvider.extractUserId(accessToken);
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Refresh Token 생성 성공 및 userId 추출 검증")
    void createRefreshTokenAndExtractUserId() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // then
        assertThat(refreshToken).isNotNull();
        
        UUID extractedUserId = jwtTokenProvider.extractUserId(refreshToken);
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효한 토큰 검증 시 true 반환")
    void validateToken_Success() {
        // given
        UUID userId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        // when
        boolean isValid = jwtTokenProvider.validateToken(accessToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 ExpiredJwtException 발생")
    void validateToken_Expired() {
        // given
        // 만료 시간을 0으로 설정하여 즉시 만료되는 토큰 생성기 세팅
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(secretKey, 0, 0);
        expiredTokenProvider.init();
        UUID userId = UUID.randomUUID();
        String expiredToken = expiredTokenProvider.createAccessToken(userId);

        // when & then
        assertThatThrownBy(() -> expiredTokenProvider.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("서명이 잘못된 토큰 검증 시 SignatureException 발생")
    void validateToken_InvalidSignature() {
        // given
        UUID userId = UUID.randomUUID();
        String originalToken = jwtTokenProvider.createAccessToken(userId);
        
        // 서명키가 다른 새로운 프로바이더 생성
        String differentSecretKey = "another-super-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha-256";
        JwtTokenProvider foreignProvider = new JwtTokenProvider(differentSecretKey, accessTokenExpiration, refreshTokenExpiration);
        foreignProvider.init();

        // when & then
        assertThatThrownBy(() -> foreignProvider.validateToken(originalToken))
                .isInstanceOf(SignatureException.class);
    }
}
