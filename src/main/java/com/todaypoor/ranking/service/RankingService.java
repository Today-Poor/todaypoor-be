package com.todaypoor.ranking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.crew.service.CrewAuthorizationService;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.AiResult;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.entity.RankingResult;
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
    private final CrewRepository crewRepository;

    public TodayRankingResult getTodayRanking(UUID userId, UUID crewId) {
        crewAuthorizationService.validateMember(crewId, userId);
        LocalDate today = LocalDate.now();

        Optional<DailyRankingEvent> eventOpt =
                dailyRankingEventRepository.findByCrewIdAndRankingDate(crewId, today);

        if (eventOpt.isEmpty() || eventOpt.get().getStatus() == RankingEventStatus.PENDING) {
            return TodayRankingResult.pending(crewId, today);
        }

        DailyRankingEvent event = eventOpt.get();
        if (event.getStatus() == RankingEventStatus.FAILED) {
            return TodayRankingResult.failed(crewId, today);
        }

        return TodayRankingResult.success(buildRankingResponse(event));
    }

    public RankingResponse getRankingByDate(UUID userId, UUID crewId, LocalDate date) {
        crewAuthorizationService.validateMember(crewId, userId);

        DailyRankingEvent event = dailyRankingEventRepository
                .findByCrewIdAndRankingDate(crewId, date)
                .filter(e -> e.getStatus() == RankingEventStatus.SUCCESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        return buildRankingResponse(event);
    }

    private RankingResponse buildRankingResponse(DailyRankingEvent event) {
        AiRankingRun run = aiRankingRunRepository
                .findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(
                        event.getId(), AiRankingRunStatus.SUCCESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        List<RankingResult> allResults =
                rankingResultRepository.findByDailyRankingEventIdOrderByRankNoAsc(event.getId());

        List<RankingResult> topResults = allResults.stream()
                .filter(r -> r.getRankNo() <= TOP_RANK_LIMIT)
                .toList();

        List<RankingResult> otherResults = allResults.stream()
                .filter(r -> r.getRankNo() > TOP_RANK_LIMIT)
                .toList();

        Map<UUID, AiResult> aiResultMap = aiResultRepository
                .findByRankingResultIdIn(topResults.stream().map(RankingResult::getId).toList())
                .stream()
                .collect(Collectors.toMap(AiResult::getRankingResultId, a -> a));

        List<RankingResponse.RankedEntry> rankings = topResults.stream()
                .map(r -> {
                    AiResult aiResult = aiResultMap.get(r.getId());
                    if (aiResult == null) {
                        throw new BusinessException(ErrorCode.RANKING_NOT_FOUND);
                    }
                    return RankingResponse.RankedEntry.of(r, aiResult);
                })
                .toList();

        List<RankingResponse.OtherEntry> others = otherResults.stream()
                .map(RankingResponse.OtherEntry::from)
                .toList();

        // TODO: BE1 User 도메인 연동 후 UserInfo(nickname, profileImageUrl) 실제 데이터로 교체
        String crewName = crewRepository.findByIdAndDeletedAtIsNull(event.getCrewId())
                .map(crew -> crew.getName())
                .orElse(null);

        return RankingResponse.of(event, crewName, run, rankings, others);
    }
}
