package com.todaypoor.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.crew.service.CrewAuthorizationService;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.repository.UserRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.AiResult;
import com.todaypoor.ranking.entity.AiResultStatus;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.entity.RankingResult;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.AiResultRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;
import com.todaypoor.ranking.repository.RankingResultRepository;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private DailyRankingEventRepository dailyRankingEventRepository;
    @Mock
    private AiRankingRunRepository aiRankingRunRepository;
    @Mock
    private RankingResultRepository rankingResultRepository;
    @Mock
    private AiResultRepository aiResultRepository;
    @Mock
    private CrewAuthorizationService crewAuthorizationService;
    @Mock
    private CrewRepository crewRepository;
    @Mock
    private UserRepository userRepository;

    // ────────────────────────────────────────────────────────────
    // getTodayRanking — PENDING
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("오늘 랭킹 이벤트가 없으면 PENDING 응답을 반환하고 이후 DB 조회를 하지 않는다.")
    void getTodayRanking_whenNoEvent_returnsPending() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(eq(crewId), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.isPending()).isTrue();
        verify(crewAuthorizationService).validateMember(crewId, userId);
        verify(aiRankingRunRepository, never()).findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("오늘 랭킹 이벤트가 PENDING 상태이면 PENDING 응답을 반환한다.")
    void getTodayRanking_whenEventIsPending_returnsPending() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        DailyRankingEvent event = DailyRankingEvent.create(crewId, LocalDate.now());

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(eq(crewId), any(LocalDate.class)))
                .willReturn(Optional.of(event));

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.isPending()).isTrue();
        assertThat(result.getPendingResponse().getCrewId()).isEqualTo(crewId);
    }

    // ────────────────────────────────────────────────────────────
    // getTodayRanking — FAILED
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("오늘 랭킹 이벤트가 FAILED 상태이면 FAILED 응답을 반환한다.")
    void getTodayRanking_whenEventIsFailed_returnsFailed() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        DailyRankingEvent event = DailyRankingEvent.create(crewId, LocalDate.now());
        event.updateStatus(RankingEventStatus.FAILED);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(eq(crewId), any(LocalDate.class)))
                .willReturn(Optional.of(event));

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.isFailed()).isTrue();
    }

    // ────────────────────────────────────────────────────────────
    // getTodayRanking — SUCCESS
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("오늘 랭킹 이벤트가 SUCCESS이면 1~3위와 순위 외 멤버를 포함한 응답을 반환한다.")
    void getTodayRanking_whenEventIsSuccess_returnsFullRankingResponse() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        DailyRankingEvent event = successEventWithId(crewId, today);
        AiRankingRun run = rankingRun(event.getId());

        RankingResult r1 = rankingResultWithId(event.getId(), run.getId(), 1, 50000);
        RankingResult r2 = rankingResultWithId(event.getId(), run.getId(), 2, 30000);
        RankingResult r3 = rankingResultWithId(event.getId(), run.getId(), 3, 20000);
        RankingResult r4 = rankingResultWithId(event.getId(), run.getId(), 4, 5000);

        List<AiResult> aiResults = List.of(
                aiResult(r1.getId()),
                aiResult(r2.getId()),
                aiResult(r3.getId())
        );

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(eq(crewId), eq(today)))
                .willReturn(Optional.of(event));
        given(aiRankingRunRepository.findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(
                event.getId(), AiRankingRunStatus.SUCCESS))
                .willReturn(Optional.of(run));
        given(rankingResultRepository.findByDailyRankingEventIdOrderByRankNoAsc(event.getId()))
                .willReturn(List.of(r1, r2, r3, r4));
        given(aiResultRepository.findByRankingResultIdIn(anyList()))
                .willReturn(aiResults);
        given(userRepository.findAllById(anyList()))
                .willReturn(List.of());
        given(crewRepository.findByIdAndDeletedAtIsNull(crewId))
                .willReturn(Optional.empty());

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.isSuccess()).isTrue();
        RankingResponse response = result.getRankingResponse();
        assertThat(response.getCrewId()).isEqualTo(crewId);
        assertThat(response.getRankingDate()).isEqualTo(today);
        assertThat(response.getRankings()).hasSize(3);
        assertThat(response.getOthers()).hasSize(1);

        assertThat(response.getRankings().get(0).getRankNo()).isEqualTo(1);
        assertThat(response.getRankings().get(0).getAiResult()).isNotNull();
        assertThat(response.getRankings().get(0).getAiResult().getRoastMessage()).isNotBlank();
        assertThat(response.getRankings().get(2).getRankNo()).isEqualTo(3);
    }

    @Test
    @DisplayName("크루 멤버 권한 검증이 항상 실행된다.")
    void getTodayRanking_alwaysCallsValidateMember() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(eq(crewId), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // when
        rankingService.getTodayRanking(userId, crewId);

        // then
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    // ────────────────────────────────────────────────────────────
    // getRankingByDate
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("특정 날짜 랭킹이 SUCCESS로 존재하면 랭킹 데이터를 반환한다.")
    void getRankingByDate_whenSuccess_returnsRankingResponse() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 20);

        DailyRankingEvent event = successEventWithId(crewId, date);
        AiRankingRun run = rankingRun(event.getId());
        RankingResult r1 = rankingResultWithId(event.getId(), run.getId(), 1, 15000);
        List<AiResult> aiResults = List.of(aiResult(r1.getId()));

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));
        given(aiRankingRunRepository.findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(
                event.getId(), AiRankingRunStatus.SUCCESS))
                .willReturn(Optional.of(run));
        given(rankingResultRepository.findByDailyRankingEventIdOrderByRankNoAsc(event.getId()))
                .willReturn(List.of(r1));
        given(aiResultRepository.findByRankingResultIdIn(anyList()))
                .willReturn(aiResults);
        given(userRepository.findAllById(anyList()))
                .willReturn(List.of());
        given(crewRepository.findByIdAndDeletedAtIsNull(crewId))
                .willReturn(Optional.empty());

        // when
        RankingResponse response = rankingService.getRankingByDate(userId, crewId, date);

        // then
        assertThat(response.getRankingDate()).isEqualTo(date);
        assertThat(response.getCrewId()).isEqualTo(crewId);
        assertThat(response.getRankings()).hasSize(1);
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    @DisplayName("특정 날짜 랭킹이 없으면 RANKING_NOT_FOUND 예외가 발생한다.")
    void getRankingByDate_whenNoEvent_throwsRankingNotFound() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 20);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rankingService.getRankingByDate(userId, crewId, date))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANKING_NOT_FOUND);
    }

    @Test
    @DisplayName("특정 날짜 랭킹이 SUCCESS가 아니면(PENDING/FAILED) RANKING_NOT_FOUND 예외가 발생한다.")
    void getRankingByDate_whenNotSuccess_throwsRankingNotFound() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 20);
        DailyRankingEvent pendingEvent = DailyRankingEvent.create(crewId, date);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(pendingEvent));

        // when & then
        assertThatThrownBy(() -> rankingService.getRankingByDate(userId, crewId, date))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANKING_NOT_FOUND);
    }

    // ────────────────────────────────────────────────────────────
    // 테스트 헬퍼
    // ────────────────────────────────────────────────────────────

    private DailyRankingEvent successEventWithId(UUID crewId, LocalDate date) {
        DailyRankingEvent event = DailyRankingEvent.create(crewId, date);
        setField(event, "id", UUID.randomUUID());
        event.updateStatus(RankingEventStatus.SUCCESS);
        return event;
    }

    private AiRankingRun rankingRun(UUID eventId) {
        AiRankingRun run = AiRankingRun.create(
                eventId, "inputData", "오늘의 월급 암살자", "금액 기준",
                "claude-haiku", 100, 200, "v1.0", AiRankingRunStatus.SUCCESS, null
        );
        setField(run, "id", UUID.randomUUID());
        return run;
    }

    private RankingResult rankingResultWithId(UUID eventId, UUID runId, int rankNo, int totalAmount) {
        RankingResult result = RankingResult.create(eventId, runId, UUID.randomUUID(), rankNo, totalAmount);
        setField(result, "id", UUID.randomUUID());
        return result;
    }

    private AiResult aiResult(UUID rankingResultId) {
        return AiResult.create(
                rankingResultId, "탕진왕", "지갑이 눈물을 흘립니다.",
                AiMode.ROAST, "inputData", "claude-haiku",
                100, 200, "v1.0", AiResultStatus.SUCCESS, null
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("테스트 필드 주입 실패: " + fieldName, e);
        }
    }
}
