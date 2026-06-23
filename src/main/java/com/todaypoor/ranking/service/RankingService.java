package com.todaypoor.ranking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.service.CrewAuthorizationService;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.AiResultRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;
import com.todaypoor.ranking.repository.RankingResultRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {
    private static final int TOP_RANK_LIMIT = 3;

    private final DailyRankingEventRepository dailyRankingEventRepository;
    private final AiRankingRunRepository aiRankingRunRepository;
    private final RankingResultRepository rankingResultRepository;
    private final AiResultRepository aiResultRepository;
    private final CrewAuthorizationService crewAuthorizationService;

    // ────────────────────────────────────────────────────────────
    // 오늘 랭킹 조회
    // ────────────────────────────────────────────────────────────

    public TodayRankingResult getTodayRanking(UUID userId, UUID crewId) {
        crewAuthorizationService.validateMember(crewId, userId);
        return TodayRankingResult.success(buildMockRankingResponse(crewId, LocalDate.now()));

        // ── 실제 로직 (AI 스케줄러 완성 후 아래 코드로 교체) ──────────────────
        // crewAuthorizationService.validateMember(crewId, userId);
        // LocalDate today = LocalDate.now();
        // Optional<DailyRankingEvent> eventOpt =
        //         dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, today);
        // if (eventOpt.isEmpty() || eventOpt.get().getStatus() == RankingEventStatus.PENDING) {
        //     return TodayRankingResult.pending(crewId, today);
        // }
        // DailyRankingEvent event = eventOpt.get();
        // if (event.getStatus() == RankingEventStatus.FAILED) {
        //     return TodayRankingResult.failed(crewId, today);
        // }
        // return TodayRankingResult.success(buildRankingResponse(event));
    }

    // ────────────────────────────────────────────────────────────
    // 특정 날짜 랭킹 조회
    // ────────────────────────────────────────────────────────────

    public RankingResponse getRankingByDate(UUID userId, UUID crewId, LocalDate date) {
        crewAuthorizationService.validateMember(crewId, userId);
        return buildMockRankingResponse(crewId, date);

        // ── 실제 로직 (AI 스케줄러 완성 후 아래 코드로 교체) ──────────────────
        // crewAuthorizationService.validateMember(crewId, userId);
        // DailyRankingEvent event = dailyRankingEventRepository
        //         .findByCrewIdAndRankingDate(crewId, date)
        //         .filter(e -> e.getStatus() == RankingEventStatus.SUCCESS)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));
        // return buildRankingResponse(event);
    }

    // ────────────────────────────────────────────────────────────
    // Mock 데이터 빌더 (AI 스케줄러 구현 후 삭제)
    // ────────────────────────────────────────────────────────────

    private RankingResponse buildMockRankingResponse(UUID crewId, LocalDate rankingDate) {
        // 1등: 세원 — COMFORT 모드
        RankingResponse.RankedEntry rank1 = RankingResponse.RankedEntry.mock(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                1,
                15_000,
                RankingResponse.UserInfo.of(
                        UUID.fromString("20000000-0000-0000-0000-000000000001"),
                        "세원",
                        null
                ),
                RankingResponse.AiResultInfo.mock(
                        UUID.fromString("30000000-0000-0000-0000-000000000001"),
                        "절약의 아이콘",
                        "국밥 한 그릇으로 하루를 버티다니, 당신은 진정한 생존자입니다.",
                        AiMode.COMFORT
                )
        );

        // 2등: 예윤 — ROAST 모드
        RankingResponse.RankedEntry rank2 = RankingResponse.RankedEntry.mock(
                UUID.fromString("10000000-0000-0000-0000-000000000002"),
                2,
                55_000,
                RankingResponse.UserInfo.of(
                        UUID.fromString("20000000-0000-0000-0000-000000000002"),
                        "예윤",
                        null
                ),
                RankingResponse.AiResultInfo.mock(
                        UUID.fromString("30000000-0000-0000-0000-000000000002"),
                        "도보 파업 선언자",
                        "가까운 거리는 좀 걸어 다니세요. 텅장 예약입니다.",
                        AiMode.ROAST
                )
        );

        // 3등: 병윤 — ROAST 모드
        RankingResponse.RankedEntry rank3 = RankingResponse.RankedEntry.mock(
                UUID.fromString("10000000-0000-0000-0000-000000000003"),
                3,
                120_000,
                RankingResponse.UserInfo.of(
                        UUID.fromString("20000000-0000-0000-0000-000000000003"),
                        "병윤",
                        null
                ),
                RankingResponse.AiResultInfo.mock(
                        UUID.fromString("30000000-0000-0000-0000-000000000003"),
                        "하루 살이 소비왕",
                        "오늘 하루만에 12만 원? 내일은 물만 드셔야겠네요.",
                        AiMode.ROAST
                )
        );

        // 순위 외: 여원 — 지출 0원
        RankingResponse.OtherEntry other1 = RankingResponse.OtherEntry.mock(
                UUID.fromString("20000000-0000-0000-0000-000000000004"),
                "여원",
                null,
                0
        );

        return RankingResponse.ofMock(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                crewId,
                "거지방 1조",
                rankingDate,
                RankingEventStatus.SUCCESS,
                "오늘의 월급 암살자",
                "금액, 소비 맥락, 불필요성을 종합해 선정",
                List.of(rank1, rank2, rank3),
                List.of(other1)
        );
    }

    // ────────────────────────────────────────────────────────────
    // 실제 DB 기반 빌더 (AI 스케줄러 완성 후 getTodayRanking / getRankingByDate 에서 호출)
    // ────────────────────────────────────────────────────────────

    // private RankingResponse buildRankingResponse(DailyRankingEvent event) {
    //     AiRankingRun run = aiRankingRunRepository
    //             .findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(
    //                     event.getId(), AiRankingRunStatus.SUCCESS)
    //             .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));
    //
    //     List<RankingResult> allResults =
    //             rankingResultRepository.findByDailyRankingEventIdOrderByRankNoAsc(event.getId());
    //
    //     List<RankingResult> topResults = allResults.stream()
    //             .filter(r -> r.getRankNo() <= TOP_RANK_LIMIT)
    //             .toList();
    //
    //     List<RankingResult> otherResults = allResults.stream()
    //             .filter(r -> r.getRankNo() > TOP_RANK_LIMIT)
    //             .toList();
    //
    //     List<UUID> topResultIds = topResults.stream()
    //             .map(RankingResult::getId)
    //             .toList();
    //
    //     Map<UUID, AiResult> aiResultMap = aiResultRepository
    //             .findByRankingResultIdIn(topResultIds)
    //             .stream()
    //             .collect(Collectors.toMap(AiResult::getRankingResultId, a -> a));
    //
    //     List<RankingResponse.RankedEntry> rankings = topResults.stream()
    //             .map(r -> {
    //                 AiResult aiResult = aiResultMap.get(r.getId());
    //                 if (aiResult == null) {
    //                     throw new BusinessException(ErrorCode.RANKING_NOT_FOUND);
    //                 }
    //                 return RankingResponse.RankedEntry.of(r, aiResult);
    //             })
    //             .toList();
    //
    //     List<RankingResponse.OtherEntry> others = otherResults.stream()
    //             .map(RankingResponse.OtherEntry::from)
    //             .toList();
    //
    //     // TODO: 크루 도메인 연동 후 실제 크루명으로 교체
    //     String crewName = "거지방 1조";
    //
    //     return RankingResponse.of(event, crewName, run, rankings, others);
    // }
}
