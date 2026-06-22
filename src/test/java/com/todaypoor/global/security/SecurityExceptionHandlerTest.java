package com.todaypoor.global.security;

import com.todaypoor.global.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityExceptionHandlerTestController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class SecurityExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private TokenProvider tokenProvider;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.todaypoor.auth.service.CustomOAuth2UserService customOAuth2UserService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    void unauthenticatedRequest_returns401WithApiResponseFormat() throws Exception {
        mockMvc.perform(get("/security-test/authenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "tester", roles = {"USER"})
    void authenticatedWithoutRole_returns403WithApiResponseFormat() throws Exception {
        mockMvc.perform(get("/security-test/admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
