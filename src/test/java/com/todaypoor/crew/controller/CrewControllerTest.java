package com.todaypoor.crew.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.todaypoor.crew.dto.request.CreateCrewRequest;
import com.todaypoor.crew.dto.request.JoinCrewRequest;
import com.todaypoor.crew.dto.response.CreateCrewResponse;
import com.todaypoor.crew.dto.response.JoinCrewResponse;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.service.CrewService;
import com.todaypoor.global.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CrewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CrewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrewService crewService;

    @Test
    void createCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CreateCrewResponse response = new CreateCrewResponse(
                crewId,
                "거지방 1조",
                "설명",
                "ABCD1234",
                now.plusDays(7),
                AiMode.ROAST,
                userId,
                now
        );

        given(crewService.createCrew(eq(userId), any(CreateCrewRequest.class))).willReturn(response);

        String requestBody = """
                {
                  "name": "거지방 1조",
                  "description": "설명",
                  "aiMode": "ROAST"
                }
                """;

        mockMvc.perform(
                        post("/api/crews")
                                .header("X-USER-ID", userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.name").value("거지방 1조"));
    }

    @Test
    void joinCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        UUID crewMemberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        JoinCrewResponse response = new JoinCrewResponse(
                crewMemberId,
                userId,
                crewId,
                CrewRole.MEMBER,
                now
        );

        given(crewService.joinCrew(eq(userId), any(JoinCrewRequest.class))).willReturn(response);

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
                .andExpect(jsonPath("$.data.crewMemberId").value(crewMemberId.toString()))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    void createCrew_validationFail_returnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "",
                  "description": "설명"
                }
                """;

        mockMvc.perform(
                        post("/api/crews")
                                .header("X-USER-ID", userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
