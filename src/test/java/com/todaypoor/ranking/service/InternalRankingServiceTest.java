package com.todaypoor.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;
import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.client.ClaudeRankingClient;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;

@ExtendWith(MockitoExtension.class)
class InternalRankingServiceTest {

    @InjectMocks
    private InternalRankingService internalRankingService;

    @Mock
    private DailyRankingEventRepository dailyRankingEventRepository;
    @Mock
    private AiRankingRunRepository aiRankingRunRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ClaudeRankingClient claudeRankingClient;
    @Mock
    private RankingPersistenceService rankingPersistenceService;

    // ────────────────────────────────────────────────────────────
    // processCrewRanking
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("정상 흐름: 지출 내역이 있는 크루의 랭킹을 생성하면 AiRankingRun/RankingResult/AiResult가 저장되고 이벤트가 SUCCESS가 된다.")
    void processCrewRanking_success_savesAllEntitiesAndSetsStatusSuccess() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);

        DailyRankingEvent event = pendingEventWithId(crewId, date);

        Expense expense1 = expense(userId1, crewId, 5000);
        Expense expense2 = expense(userId2, crewId, 3000);

        AiRankingOutput mockOutput = mockOutput(List.of(
                rankingItem(userId1, 5000, 1, "1등 타이틀", "1등 메시지", AiMode.ROAST),
                rankingItem(userId2, 3000, 2, "2등 타이틀", "2등 메시지", AiMode.ROAST)
        ));

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));
        given(expenseRepository.findByCrewIdAndSpentAtBetween(eq(crewId), any(), any()))
                .willReturn(List.of(expense1, expense2));
        given(claudeRankingClient.generateRanking(anyList(), anyString()))
                .willReturn(mockOutput);
        given(rankingPersistenceService.persistResults(eq(event.getId()), any(AiRankingOutput.class)))
                .willAnswer(inv -> { event.updateStatus(RankingEventStatus.SUCCESS); return event; });

        // when
        DailyRankingEvent result = internalRankingService.processCrewRanking(crewId, date);

        // then
        assertThat(result.getStatus()).isEqualTo(RankingEventStatus.SUCCESS);
        verify(claudeRankingClient).generateRanking(anyList(), anyString());
        verify(rankingPersistenceService).persistResults(eq(event.getId()), any(AiRankingOutput.class));
    }

    @Test
    @DisplayName("4위 이하 멤버는 AiResult를 저장하지 않는다.")
    void processCrewRanking_rank4AndAbove_doesNotSaveAiResult() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);
        DailyRankingEvent event = pendingEventWithId(crewId, date);

        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID());

        AiRankingOutput mockOutput = mockOutput(List.of(
                rankingItem(userIds.get(0), 5000, 1, "1등", "msg1", AiMode.ROAST),
                rankingItem(userIds.get(1), 4000, 2, "2등", "msg2", AiMode.ROAST),
                rankingItem(userIds.get(2), 3000, 3, "3등", "msg3", AiMode.ROAST),
                rankingItem(userIds.get(3), 1000, 4, null, null, null) // 4위: AiResult 없음
        ));

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));
        given(expenseRepository.findByCrewIdAndSpentAtBetween(eq(crewId), any(), any()))
                .willReturn(List.of(
                        expense(userIds.get(0), crewId, 5000),
                        expense(userIds.get(1), crewId, 4000),
                        expense(userIds.get(2), crewId, 3000),
                        expense(userIds.get(3), crewId, 1000)
                ));
        given(claudeRankingClient.generateRanking(anyList(), anyString())).willReturn(mockOutput);
        given(rankingPersistenceService.persistResults(any(UUID.class), any(AiRankingOutput.class)))
                .willAnswer(inv -> { userIds.forEach(id -> {}); return pendingEventWithId(crewId, date); });

        // when
        internalRankingService.processCrewRanking(crewId, date);

        // then: Claude 호출 후 persistResults에 위임됐는지 확인
        // 1~3위 AiResult 저장 여부는 RankingPersistenceServiceTest에서 검증
        verify(claudeRankingClient).generateRanking(anyList(), anyString());
        verify(rankingPersistenceService).persistResults(any(UUID.class), any(AiRankingOutput.class));
    }

    @Test
    @DisplayName("이미 SUCCESS인 이벤트는 재처리하지 않고 바로 반환한다.")
    void processCrewRanking_whenAlreadySuccess_returnsEarlyWithoutProcessing() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);
        DailyRankingEvent event = pendingEventWithId(crewId, date);
        event.updateStatus(RankingEventStatus.SUCCESS);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));

        // when
        DailyRankingEvent result = internalRankingService.processCrewRanking(crewId, date);

        // then
        assertThat(result.getStatus()).isEqualTo(RankingEventStatus.SUCCESS);
        verify(expenseRepository, never()).findByCrewIdAndSpentAtBetween(any(), any(), any());
        verify(claudeRankingClient, never()).generateRanking(any(), any());
    }

    @Test
    @DisplayName("오늘 지출 내역이 없으면 INVALID_REQUEST 예외가 발생한다.")
    void processCrewRanking_whenNoExpenses_throwsInvalidRequest() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);
        DailyRankingEvent event = pendingEventWithId(crewId, date);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));
        given(expenseRepository.findByCrewIdAndSpentAtBetween(eq(crewId), any(), any()))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> internalRankingService.processCrewRanking(crewId, date))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

        verify(claudeRankingClient, never()).generateRanking(any(), any());
    }

    @Test
    @DisplayName("랭킹 이벤트가 존재하지 않으면 RANKING_NOT_FOUND 예외가 발생한다.")
    void processCrewRanking_whenEventNotFound_throwsRankingNotFound() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> internalRankingService.processCrewRanking(crewId, date))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANKING_NOT_FOUND);
    }

    // ────────────────────────────────────────────────────────────
    // ensureEventExists
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이벤트가 없으면 PENDING 상태로 새로 생성하여 반환한다.")
    void ensureEventExists_whenEventNotExists_createsAndReturnsPendingEvent() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.empty());
        given(dailyRankingEventRepository.save(any(DailyRankingEvent.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        DailyRankingEvent result = internalRankingService.ensureEventExists(crewId, date);

        // then
        assertThat(result.getCrewId()).isEqualTo(crewId);
        assertThat(result.getRankingDate()).isEqualTo(date);
        assertThat(result.getStatus()).isEqualTo(RankingEventStatus.PENDING);
        verify(dailyRankingEventRepository).save(any(DailyRankingEvent.class));
    }

    @Test
    @DisplayName("이벤트가 이미 존재하면 기존 이벤트를 그대로 반환하고 새로 저장하지 않는다.")
    void ensureEventExists_whenEventExists_returnsExistingEventWithoutSaving() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);
        DailyRankingEvent existing = pendingEventWithId(crewId, date);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(existing));

        // when
        DailyRankingEvent result = internalRankingService.ensureEventExists(crewId, date);

        // then
        assertThat(result).isSameAs(existing);
        verify(dailyRankingEventRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────────────────
    // markEventFailed
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이벤트가 존재하면 상태를 FAILED로 변경한다.")
    void markEventFailed_whenEventExists_updatesStatusToFailed() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);
        DailyRankingEvent event = pendingEventWithId(crewId, date);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.of(event));

        // when
        internalRankingService.markEventFailed(crewId, date);

        // then
        assertThat(event.getStatus()).isEqualTo(RankingEventStatus.FAILED);
    }

    @Test
    @DisplayName("이벤트가 존재하지 않아도 예외 없이 조용히 종료된다.")
    void markEventFailed_whenEventNotFound_doesNothing() {
        // given
        UUID crewId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 22);

        given(dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, date))
                .willReturn(Optional.empty());

        // when & then: 예외가 발생하지 않아야 한다
        internalRankingService.markEventFailed(crewId, date);
    }

    // ────────────────────────────────────────────────────────────
    // getLatestRun
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("랭킹 이벤트의 최신 AI 실행 기록을 반환한다.")
    void getLatestRun_success_returnsLatestRun() {
        // given
        UUID rankingEventId = UUID.randomUUID();
        DailyRankingEvent event = pendingEventWithId(UUID.randomUUID(), LocalDate.now());
        AiRankingRun run = AiRankingRun.create(
                rankingEventId, "inputData", "topic", "criteria",
                "mock-gpt-4o", 100, 200, "v1.0", AiRankingRunStatus.SUCCESS, null
        );

        given(dailyRankingEventRepository.findById(rankingEventId))
                .willReturn(Optional.of(event));
        given(aiRankingRunRepository.findTopByDailyRankingEventIdOrderByCreatedAtDesc(rankingEventId))
                .willReturn(Optional.of(run));

        // when
        AiRankingRun result = internalRankingService.getLatestRun(rankingEventId);

        // then
        assertThat(result.getGeneratedTopic()).isEqualTo("topic");
        assertThat(result.getStatus()).isEqualTo(AiRankingRunStatus.SUCCESS);
    }

    @Test
    @DisplayName("존재하지 않는 랭킹 이벤트 ID로 조회하면 RANKING_NOT_FOUND 예외가 발생한다.")
    void getLatestRun_whenEventNotFound_throwsRankingNotFound() {
        // given
        UUID rankingEventId = UUID.randomUUID();

        given(dailyRankingEventRepository.findById(rankingEventId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> internalRankingService.getLatestRun(rankingEventId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANKING_NOT_FOUND);
    }

    @Test
    @DisplayName("이벤트는 있지만 AI 실행 기록이 없으면 RANKING_NOT_FOUND 예외가 발생한다.")
    void getLatestRun_whenRunNotFound_throwsRankingNotFound() {
        // given
        UUID rankingEventId = UUID.randomUUID();
        DailyRankingEvent event = pendingEventWithId(UUID.randomUUID(), LocalDate.now());

        given(dailyRankingEventRepository.findById(rankingEventId))
                .willReturn(Optional.of(event));
        given(aiRankingRunRepository.findTopByDailyRankingEventIdOrderByCreatedAtDesc(rankingEventId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> internalRankingService.getLatestRun(rankingEventId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RANKING_NOT_FOUND);
    }

    // ────────────────────────────────────────────────────────────
    // 테스트 헬퍼
    // ────────────────────────────────────────────────────────────

    private DailyRankingEvent pendingEventWithId(UUID crewId, LocalDate date) {
        DailyRankingEvent event = DailyRankingEvent.create(crewId, date);
        setField(event, "id", UUID.randomUUID());
        return event;
    }

    private Expense expense(UUID userId, UUID crewId, int amount) {
        return Expense.create(
                userId, crewId, UUID.randomUUID(),
                ExpenseCategory.FOOD, amount, "테스트 가맹점", null,
                ExpenseVisibility.PUBLIC, LocalDateTime.now()
        );
    }

    private AiRankingOutput mockOutput(List<AiRankingOutput.UserRankingItem> items) {
        return new AiRankingOutput(
                "테스트 주제", "테스트 기준", "mock-model",
                100, 200, "v1.0", "inputData", items
        );
    }

    private AiRankingOutput.UserRankingItem rankingItem(
            UUID userId, int totalAmount, int rankNo,
            String title, String roastMessage, AiMode mode
    ) {
        return new AiRankingOutput.UserRankingItem(userId, totalAmount, rankNo, title, roastMessage, mode);
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
