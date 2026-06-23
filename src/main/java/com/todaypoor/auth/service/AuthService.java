package com.todaypoor.auth.service;

import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.global.security.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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
        try {
            // 1. 유효성 검증
            tokenProvider.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 재발급 실패: 만료된 Refresh Token입니다.");
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 재발급 실패: 유효하지 않은 Refresh Token입니다.");
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. userId 추출
        UUID userId = tokenProvider.extractUserId(refreshToken);

        // 3. 새 Access Token & Refresh Token 생성 (RTR 구조)
        String newAccessToken = tokenProvider.createAccessToken(userId);
        String newRefreshToken = tokenProvider.createRefreshToken(userId);

        log.info("토큰 재발급 성공. 유저 ID: {}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
