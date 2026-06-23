package com.todaypoor.crew.controller;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.global.config.JacksonConfig;
import com.todaypoor.crew.service.CrewMemberService;
import com.todaypoor.global.exception.GlobalExceptionHandler;
import com.todaypoor.global.security.CustomUserDetails;
import com.todaypoor.user.entity.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CrewMemberController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, JacksonConfig.class, CrewMemberControllerTest.TestSecurityConfig.class})
class CrewMemberControllerTest {

    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrewMemberService crewMemberService;

    @Test
    void joinCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        JoinCrewResponse response = new JoinCrewResponse(
                crewId,
                "거지방 1조",
                CrewRole.MEMBER,
                4,
                5,
                now
        );

        given(crewMemberService.joinCrew(eq(userId), any(JoinCrewRequest.class))).willReturn(response);

        String requestBody = """
                {
                  "inviteCode": "ABCD1234"
                }
                """;

        mockMvc.perform(
                        withAuth(post("/api/crews/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody), userId)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.crewName").value("거지방 1조"));
    }

    @Test
    void getCrewMembers_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CrewMemberListResponse.CrewMemberList member = new CrewMemberListResponse.CrewMemberList(
                userId,
                null,
                CrewRole.OWNER,
                now
        );
        CrewMemberListResponse response = new CrewMemberListResponse(
                crewId,
                "거지방 1조",
                List.of(member)
        );

        given(crewMemberService.getCrewMembers(userId, crewId)).willReturn(response);

        mockMvc.perform(
                        withAuth(get("/api/crews/{crewId}/members", crewId), userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.members[0].userId").value(userId.toString()));
    }

    @Test
    void getCrewMemberDetail_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        UUID targetMemberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CrewMemberDetailResponse response = new CrewMemberDetailResponse(
                crewId,
                targetMemberId,
                null,
                CrewRole.MEMBER,
                now
        );

        given(crewMemberService.getCrewMemberDetail(userId, crewId, targetMemberId)).willReturn(response);

        mockMvc.perform(
                        withAuth(get("/api/crews/{crewId}/members/{memberId}", crewId, targetMemberId), userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(targetMemberId.toString()));
    }

    @Test
    void leaveCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();

        mockMvc.perform(
                        withAuth(delete("/api/crews/{crewId}/members/me", crewId), userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(crewMemberService).leaveCrew(userId, crewId);
    }

    @Test
    void removeCrewMember_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        UUID targetMemberId = UUID.randomUUID();

        mockMvc.perform(
                        withAuth(delete("/api/crews/{crewId}/members/{memberId}", crewId, targetMemberId), userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(crewMemberService).removeCrewMember(userId, crewId, targetMemberId);
    }

    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder, UUID userId) {
        CustomUserDetails userDetails = mockUserDetails(userId);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        return builder.with(SecurityMockMvcRequestPostProcessors.authentication(auth));
    }

    private CustomUserDetails mockUserDetails(UUID userId) {
        User user = User.create("테스트유저");
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return new CustomUserDetails(user);
    }
}
