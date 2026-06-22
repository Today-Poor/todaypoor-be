package com.todaypoor.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todaypoor.crew.service.CrewAuthorizationService;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.entity.RankingEventStatus;
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

    @Test
    @DisplayName("오늘 랭킹 조회 시 Mock 데이터로 SUCCESS 상태와 1~3위 + 순위 외 멤버를 반환한다.")
    void getTodayRanking_success_returnsMockDataWithCorrectStructure() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.isSuccess()).isTrue();

        RankingResponse response = result.getRankingResponse();
        assertThat(response.getCrewId()).isEqualTo(crewId);
        assertThat(response.getStatus()).isEqualTo(RankingEventStatus.SUCCESS);
        assertThat(response.getRankings()).hasSize(3);
        assertThat(response.getOthers()).hasSize(1);

        // 1~3위 순서 검증
        assertThat(response.getRankings().get(0).getRankNo()).isEqualTo(1);
        assertThat(response.getRankings().get(1).getRankNo()).isEqualTo(2);
        assertThat(response.getRankings().get(2).getRankNo()).isEqualTo(3);

        // 각 순위에 AI 피드백이 존재하는지 검증
        assertThat(response.getRankings().get(0).getAiResult()).isNotNull();
        assertThat(response.getRankings().get(0).getAiResult().getRoastMessage()).isNotBlank();
    }

    @Test
    @DisplayName("오늘 랭킹 조회 시 반환된 랭킹 날짜가 오늘 날짜이다.")
    void getTodayRanking_rankingDateIsToday() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        // when
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        // then
        assertThat(result.getRankingResponse().getRankingDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("오늘 랭킹 조회 시 크루 멤버 권한 검증이 실행된다.")
    void getTodayRanking_callsValidateMember() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when
        rankingService.getTodayRanking(userId, crewId);

        // then
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    @DisplayName("특정 날짜 랭킹 조회 시 요청한 날짜의 랭킹을 반환한다.")
    void getRankingByDate_success_returnsDataWithRequestedDate() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 5, 20);

        // when
        RankingResponse response = rankingService.getRankingByDate(userId, crewId, targetDate);

        // then
        assertThat(response.getRankingDate()).isEqualTo(targetDate);
        assertThat(response.getCrewId()).isEqualTo(crewId);
        assertThat(response.getRankings()).hasSize(3);
        assertThat(response.getOthers()).hasSize(1);
    }

    @Test
    @DisplayName("특정 날짜 랭킹 조회 시 크루 멤버 권한 검증이 실행된다.")
    void getRankingByDate_callsValidateMember() {
        // given
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 20);

        // when
        rankingService.getRankingByDate(userId, crewId, date);

        // then
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }
}
