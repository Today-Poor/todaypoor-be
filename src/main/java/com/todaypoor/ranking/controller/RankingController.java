package com.todaypoor.ranking.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.security.CustomUserDetails;
import com.todaypoor.ranking.dto.TodayRankingResult;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "소비 랭킹 (Ranking)", description = "크루 내 소비 지출 일일 랭킹 및 피드백 조회 API")
@RestController
@RequestMapping("/api/crews/{crewId}/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "오늘의 소비 랭킹 조회", description = "현재 크루의 오늘의 소비 랭킹 및 AI 피드백을 조회합니다. 상태에 따라 성공(SUCCESS), 대기(PENDING), 실패(FAILED) 응답을 반환합니다.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<?>> getTodayRanking(
            @PathVariable UUID crewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TodayRankingResult result = rankingService.getTodayRanking(userDetails.getUserId(), crewId);

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

    @Operation(summary = "특정 날짜의 소비 랭킹 조회", description = "특정 크루의 지정한 날짜의 소비 랭킹 및 AI 피드백을 조회합니다. 성공(SUCCESS) 상태인 랭킹만 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<RankingResponse>> getRankingByDate(
            @PathVariable UUID crewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        RankingResponse response = rankingService.getRankingByDate(userDetails.getUserId(), crewId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
