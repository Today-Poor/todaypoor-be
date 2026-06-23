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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "내부 관리자 (Internal Admin)", description = "일일 랭킹 생성 강제화 및 재시도 등을 처리하는 내부 시스템 API")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalRankingController {

    private final InternalRankingService internalRankingService;
    private final DailyRankingEventRepository dailyRankingEventRepository;

    @Operation(summary = "크루 일일 랭킹 강제 생성", description = "특정 크루의 오늘의 일일 랭킹을 즉시 강제로 생성 처리합니다.")
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

    @Operation(summary = "랭킹 생성 이벤트 재시도", description = "실패(FAILED) 혹은 대기(PENDING) 상태인 랭킹 생성 이벤트를 재시도 처리합니다.")
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

    @Operation(summary = "최신 AI 실행 기록 조회", description = "지정한 랭킹 이벤트와 관련된 AI API 실행 상세 내역(입출력 토큰, 프롬프트 로그 등)을 조회합니다.")
    @GetMapping("/ranking-events/{rankingEventId}/run")
    public ResponseEntity<ApiResponse<AiRankingRunResponse>> getRankingRun(
            @PathVariable UUID rankingEventId
    ) {
        AiRankingRun run = internalRankingService.getLatestRun(rankingEventId);
        return ResponseEntity.ok(ApiResponse.success(AiRankingRunResponse.from(run)));
    }
}
