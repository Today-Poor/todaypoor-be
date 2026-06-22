package com.todaypoor.auth.service;

import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.global.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("유효한 Refresh Token으로 reissue 요청 시 새로운 Access/Refresh Token을 발급한다")
    void reissue_Success() {
        // given
        String oldRefreshToken = "old-refresh-token";
        UUID userId = UUID.randomUUID();
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        given(tokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(tokenProvider.extractUserId(oldRefreshToken)).willReturn(userId);
        given(tokenProvider.createAccessToken(userId)).willReturn(newAccessToken);
        given(tokenProvider.createRefreshToken(userId)).willReturn(newRefreshToken);

        // when
        TokenResponse response = authService.reissue(oldRefreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);

        verify(tokenProvider, times(1)).validateToken(oldRefreshToken);
        verify(tokenProvider, times(1)).extractUserId(oldRefreshToken);
        verify(tokenProvider, times(1)).createAccessToken(userId);
        verify(tokenProvider, times(1)).createRefreshToken(userId);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 reissue 요청 시 예외를 던진다")
    void reissue_Fail_InvalidToken() {
        // given
        String invalidToken = "invalid-token";
        doThrow(new IllegalArgumentException("Invalid token")).when(tokenProvider).validateToken(invalidToken);

        // when & then
        assertThatThrownBy(() -> authService.reissue(invalidToken))
                .isInstanceOf(IllegalArgumentException.class);

        verify(tokenProvider, times(1)).validateToken(invalidToken);
        verify(tokenProvider, never()).extractUserId(any());
    }
}
