package com.todaypoor.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaypoor.auth.dto.TokenReissueRequest;
import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.auth.service.AuthService;
import com.todaypoor.global.config.SecurityConfig;
import com.todaypoor.global.security.CustomAccessDeniedHandler;
import com.todaypoor.global.security.CustomAuthenticationEntryPoint;
import com.todaypoor.global.security.CustomUserDetailsService;
import com.todaypoor.global.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    // SecurityConfig 로딩을 만족하기 위한 MockitoBean들
    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private com.todaypoor.auth.service.CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private com.todaypoor.global.security.OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private com.todaypoor.global.security.OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    @DisplayName("Request Body에 Refresh Token을 담아 토큰 재발급 요청 시 200 OK와 신규 토큰 세트를 응답한다")
    void reissue_RequestBody_Success() throws Exception {
        // given
        TokenReissueRequest request = new TokenReissueRequest("valid-refresh-token");
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token");

        given(authService.reissue(anyString())).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .with(csrf()) // CSRF 토큰 우회/적용 설정
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("Authorization Header에 Refresh Token을 담아 토큰 재발급 요청 시 200 OK와 신규 토큰 세트를 응답한다")
    void reissue_Header_Success() throws Exception {
        // given
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token");

        given(authService.reissue("valid-refresh-token")).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .with(csrf())
                        .header("Authorization", "Bearer valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }
}
