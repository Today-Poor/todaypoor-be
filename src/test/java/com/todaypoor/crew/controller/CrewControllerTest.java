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

import com.todaypoor.crew.dto.crew.request.CreateCrewRequest;
import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.request.UpdateCrewRequest;
import com.todaypoor.crew.dto.crew.response.CreateCrewResponse;
import com.todaypoor.crew.dto.crew.response.CrewDetailResponse;
import com.todaypoor.crew.dto.crew.response.CrewMainResponse;
import com.todaypoor.crew.dto.crew.response.InviteCodeResponse;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crew.response.MyCrewListResponse;
import com.todaypoor.crew.dto.crew.response.UpdateCrewResponse;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;
import com.todaypoor.global.config.JacksonConfig;
import com.todaypoor.crew.service.CrewService;
import com.todaypoor.global.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CrewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
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
        CreateCrewResponse.Owner owner = new CreateCrewResponse.Owner(
                userId,
                "철수",
                "https://image-url.com/profile.png"
        );

        CreateCrewResponse response = new CreateCrewResponse(
                crewId,
                "거지방 1조",
                "설명",
                5,
                1,
                AiMode.ROAST,
                "ABCD1234",
                now.plusDays(7),
                owner,
                now
        );

        given(crewService.createCrew(eq(userId), any(CreateCrewRequest.class))).willReturn(response);

        String requestBody = """
                {
                  "name": "거지방 1조",
                  "description": "설명",
                  "maxMemberCount": 5,
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
        LocalDateTime now = LocalDateTime.now();

        JoinCrewResponse response = new JoinCrewResponse(
                crewId,
                "거지방 1조",
                CrewRole.MEMBER,
                4,
                5,
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
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.crewName").value("거지방 1조"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    void updateCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UpdateCrewResponse response = new UpdateCrewResponse(
                crewId,
                "수정된 크루",
                null,
                4,
                2,
                AiMode.COMFORT,
                now
        );

        given(crewService.updateCrew(eq(userId), eq(crewId), any(UpdateCrewRequest.class))).willReturn(response);

        String requestBody = """
                {
                  "name": "수정된 크루",
                  "description": null,
                  "maxMemberCount": 4,
                  "aiMode": "COMFORT"
                }
                """;

        mockMvc.perform(
                        patch("/api/crews/{crewId}", crewId)
                                .header("X-USER-ID", userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.name").value("수정된 크루"))
                .andExpect(jsonPath("$.data.maxMemberCount").value(4))
                .andExpect(jsonPath("$.data.currentMemberCount").value(2))
                .andExpect(jsonPath("$.data.aiMode").value("COMFORT"));
    }

    @Test
    void reissueInviteCode_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        InviteCodeResponse response = new InviteCodeResponse(crewId, "NEW12345", expiresAt);

        given(crewService.reissueInviteCode(userId, crewId)).willReturn(response);

        mockMvc.perform(
                        post("/api/crews/{crewId}/invite-code/reissue", crewId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.inviteCode").value("NEW12345"));
    }

    @Test
    void getInviteCode_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        InviteCodeResponse response = new InviteCodeResponse(crewId, "ABCD1234", expiresAt);

        given(crewService.getInviteCode(userId, crewId)).willReturn(response);

        mockMvc.perform(
                        post("/api/crews/{crewId}/invite-code", crewId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.inviteCode").value("ABCD1234"));
    }

    @Test
    void getMyCrews_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        MyCrewListResponse.MyCrewSummary summary = new MyCrewListResponse.MyCrewSummary(
                crewId,
                "거지방 1조",
                "설명",
                AiMode.ROAST,
                CrewRole.OWNER,
                1,
                5,
                now
        );
        MyCrewListResponse response = new MyCrewListResponse(List.of(summary));

        given(crewService.getMyCrews(userId)).willReturn(response);

        mockMvc.perform(
                        get("/api/crews")
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crews[0].crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.crews[0].name").value("거지방 1조"))
                .andExpect(jsonPath("$.data.crews[0].role").value("OWNER"));
    }

    @Test
    void getCrewDetail_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CrewDetailResponse.Owner owner = new CrewDetailResponse.Owner(userId, null, null);
        CrewDetailResponse response = new CrewDetailResponse(
                crewId,
                "거지방 1조",
                "설명",
                AiMode.ROAST,
                2,
                5,
                "ABCD1234",
                now.plusDays(7),
                owner,
                now,
                now
        );

        given(crewService.getCrewDetail(userId, crewId)).willReturn(response);

        mockMvc.perform(
                        get("/api/crews/{crewId}/detail", crewId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.name").value("거지방 1조"))
                .andExpect(jsonPath("$.data.owner.userId").value(userId.toString()));
    }

    @Test
    void deleteCrew_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/crews/{crewId}", crewId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"));

        verify(crewService).deleteCrew(userId, crewId);
    }

    @Test
    void getCrewMain_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        LocalDateTime spentAt = LocalDateTime.of(2026, 5, 20, 13, 10);
        CrewMainResponse.LatestExpense latestExpense = new CrewMainResponse.LatestExpense(
                expenseId,
                ExpenseCategory.CAFE,
                5800,
                ExpenseVisibility.PUBLIC,
                spentAt
        );
        CrewMainResponse.MemberSummary member = new CrewMainResponse.MemberSummary(
                userId,
                null,
                null,
                CrewRole.OWNER,
                latestExpense
        );
        CrewMainResponse response = new CrewMainResponse(
                crewId,
                "거지방 1조",
                1,
                5,
                List.of(member)
        );

        given(crewService.getCrewMain(userId, crewId)).willReturn(response);

        mockMvc.perform(
                        get("/api/crews/{crewId}", crewId)
                                .header("X-USER-ID", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.crewId").value(crewId.toString()))
                .andExpect(jsonPath("$.data.crewName").value("거지방 1조"))
                .andExpect(jsonPath("$.data.members[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.members[0].latestExpense.expenseId").value(expenseId.toString()));
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
