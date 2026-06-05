package com.todaypoor.crew.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.global.config.JacksonConfig;
import com.todaypoor.crew.service.CrewMemberService;
import com.todaypoor.global.exception.GlobalExceptionHandler;

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
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
class CrewMemberControllerTest {

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
                        post("/api/crews/join")
                                .header("X-USER-ID", userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
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
                        get("/api/crews/{crewId}/members", crewId)
                                .header("X-USER-ID", userId.toString())
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
                null,
                CrewRole.MEMBER,
                now
        );

        given(crewMemberService.getCrewMemberDetail(userId, crewId, targetMemberId)).willReturn(response);

        mockMvc.perform(
                        get("/api/crews/{crewId}/members/{memberId}", crewId, targetMemberId)
                                .header("X-USER-ID", userId.toString())
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
                        delete("/api/crews/{crewId}/members/me", crewId)
                                .header("X-USER-ID", userId.toString())
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
                        delete("/api/crews/{crewId}/members/{memberId}", crewId, targetMemberId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(crewMemberService).removeCrewMember(userId, crewId, targetMemberId);
    }
}
