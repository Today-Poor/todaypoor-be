package com.todaypoor.ranking.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.ranking.dto.internal.response.AiRankingRunResponse;
import com.todaypoor.ranking.dto.internal.response.RankingGenerateResponse;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;
import com.todaypoor.ranking.service.InternalRankingService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalRankingController {

    private final InternalRankingService internalRankingService;
    private final DailyRankingEventRepository dailyRankingEventRepository;

    /**
     * 특정 크루의 오늘 일일 랭킹을 강제 생성한다.
     * 이미 SUCCESS 상태라면 기존 이벤트를 그대로 반환한다.
     */
    @PostMapping("/crews/{crewId}/rankings")
    public ResponseEntity<ApiResponse<RankingGenerateResponse>> generateCrewRanking(
            @PathVariable UUID crewId
    ) {
        LocalDate today = LocalDate.now();

        // 이벤트 선생성 후 랭킹 처리
        internalRankingService.ensureEventExists(crewId, today);
        DailyRankingEvent event = internalRankingService.processCrewRanking(crewId, today);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(RankingGenerateResponse.from(event)));
    }

    /**
     * FAILED 또는 PENDING 상태인 랭킹 이벤트를 재시도한다.
     */
    @PostMapping("/ranking-events/{rankingEventId}/retry")
    public ResponseEntity<ApiResponse<RankingGenerateResponse>> retryRankingEvent(
            @PathVariable UUID rankingEventId
    ) {
        DailyRankingEvent event = dailyRankingEventRepository.findById(rankingEventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        if (event.getStatus() == RankingEventStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        try {
            DailyRankingEvent updated = internalRankingService.processCrewRanking(
                    event.getCrewId(), event.getRankingDate()
            );
            return ResponseEntity.ok(ApiResponse.success(RankingGenerateResponse.from(updated)));
        } catch (Exception e) {
            internalRankingService.markEventFailed(event.getCrewId(), event.getRankingDate());
            throw new BusinessException(ErrorCode.RANKING_GENERATION_FAILED);
        }
    }

    /**
     * 특정 랭킹 이벤트의 최신 AI 실행 기록(토큰 수, 프롬프트 내용 등)을 조회한다.
     */
    @GetMapping("/ranking-events/{rankingEventId}/run")
    public ResponseEntity<ApiResponse<AiRankingRunResponse>> getRankingRun(
            @PathVariable UUID rankingEventId
    ) {
        AiRankingRun run = internalRankingService.getLatestRun(rankingEventId);
        return ResponseEntity.ok(ApiResponse.success(AiRankingRunResponse.from(run)));
    }
}
