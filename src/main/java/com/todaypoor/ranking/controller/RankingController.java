package com.todaypoor.ranking.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.service.RankingService;

@RestController
@RequestMapping("/api/crews/{crewId}/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 오늘 랭킹 조회.
     * 상태에 따라 세 가지 응답을 모두 200 OK로 반환한다.
     * - SUCCESS  : 전체 랭킹 데이터 (1~3위 + 나머지)
     * - PENDING  : 아직 생성 전, 예상 오픈 시각 안내
     * - FAILED   : 생성 실패
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<?>> getTodayRanking(
            @PathVariable UUID crewId,
            @RequestHeader("X-USER-ID") UUID userId
    ) {
        TodayRankingResult result = rankingService.getTodayRanking(userId, crewId);

        if (result.isPending()) {
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "RANKING_NOT_READY",
                    "아직 오늘의 랭킹이 생성되지 않았습니다.",
                    result.getPendingResponse()
            ));
        }

        if (result.isFailed()) {
            return ResponseEntity.ok(ApiResponse.fail(
                    "RANKING_GENERATION_FAILED",
                    "랭킹 생성에 실패했습니다.",
                    result.getFailedResponse()
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(result.getRankingResponse()));
    }

    /**
     * 특정 날짜 랭킹 조회.
     * SUCCESS 상태의 랭킹만 반환하며, 없으면 RANKING_NOT_FOUND 에러가 발생한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RankingResponse>> getRankingByDate(
            @PathVariable UUID crewId,
            @RequestHeader("X-USER-ID") UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        RankingResponse response = rankingService.getRankingByDate(userId, crewId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
