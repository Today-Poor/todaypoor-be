package com.todaypoor.auth.service;

import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;

    /**
     * Refresh Token을 검증하고, 유효한 경우 새 Access Token과 Refresh Token(RTR)을 발급합니다.
     *
     * @param refreshToken 검증할 Refresh Token
     * @return 새로 발급된 토큰 세트
     */
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // 1. 유효성 검증
        tokenProvider.validateToken(refreshToken);

        // 2. userId 추출
        UUID userId = tokenProvider.extractUserId(refreshToken);

        // 3. 새 Access Token & Refresh Token 생성 (RTR 구조)
        String newAccessToken = tokenProvider.createAccessToken(userId);
        String newRefreshToken = tokenProvider.createRefreshToken(userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
