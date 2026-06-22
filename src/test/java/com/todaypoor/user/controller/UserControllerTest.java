package com.todaypoor.user.controller;

import com.todaypoor.global.config.SecurityConfig;
import com.todaypoor.global.security.*;
import com.todaypoor.user.dto.UserMeResponse;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // SecurityConfig 구동용 MockitoBean들
    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private com.todaypoor.auth.service.CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    @DisplayName("로그인된 사용자가 내 정보 조회를 요청 시 200 OK와 핵심 프로필 정보를 반환한다")
    void getMyInfo_Success() throws Exception {
        // given
        UUID targetUserId = UUID.randomUUID();
        String nickname = "tester";
        
        // Mock User 생성하여 ID와 닉네임 강제 반환
        User mockUser = org.mockito.Mockito.mock(User.class);
        given(mockUser.getId()).willReturn(targetUserId);
        given(mockUser.getNickname()).willReturn(nickname);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        UserMeResponse expectedResponse = UserMeResponse.builder()
                .userId(targetUserId)
                .nickname(nickname)
                .build();

        given(userService.getMyInfo(any(UUID.class))).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/user/me")
                        .with(authentication(authenticationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.userId").value(targetUserId.toString()))
                .andExpect(jsonPath("$.data.nickname").value(nickname));
    }
}
