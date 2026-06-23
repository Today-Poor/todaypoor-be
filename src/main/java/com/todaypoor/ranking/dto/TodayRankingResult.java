package com.todaypoor.ranking.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;

import com.todaypoor.ranking.dto.response.RankingFailedResponse;
import com.todaypoor.ranking.dto.response.RankingPendingResponse;
import com.todaypoor.ranking.dto.response.RankingResponse;
import com.todaypoor.ranking.entity.RankingEventStatus;

/**
 * 오늘 랭킹 조회 결과의 상태를 표현하는 서비스→컨트롤러 전달 객체.
 * 오늘 랭킹은 PENDING/FAILED/SUCCESS 세 가지 상태를 모두 200 OK로 반환하기 때문에
 * 상태별로 다른 응답 DTO를 담아 전달한다.
 */
@Getter
public class TodayRankingResult {

    private final RankingEventStatus status;
    private final RankingResponse rankingResponse;       // SUCCESS 상태일 때만 non-null
    private final RankingPendingResponse pendingResponse; // PENDING 상태일 때만 non-null
    private final RankingFailedResponse failedResponse;  // FAILED 상태일 때만 non-null

    private TodayRankingResult(
            RankingEventStatus status,
            RankingResponse rankingResponse,
            RankingPendingResponse pendingResponse,
            RankingFailedResponse failedResponse
    ) {
        this.status = status;
        this.rankingResponse = rankingResponse;
        this.pendingResponse = pendingResponse;
        this.failedResponse = failedResponse;
    }

    public static TodayRankingResult pending(UUID crewId, LocalDate date) {
        return new TodayRankingResult(
                RankingEventStatus.PENDING,
                null,
                RankingPendingResponse.of(crewId, date),
                null
        );
    }

    public static TodayRankingResult failed(UUID crewId, LocalDate date) {
        return new TodayRankingResult(
                RankingEventStatus.FAILED,
                null,
                null,
                RankingFailedResponse.of(crewId, date)
        );
    }

    public static TodayRankingResult success(RankingResponse response) {
        return new TodayRankingResult(RankingEventStatus.SUCCESS, response, null, null);
    }

    public boolean isPending() {
        return status == RankingEventStatus.PENDING;
    }

    public boolean isFailed() {
        return status == RankingEventStatus.FAILED;
    }

    public boolean isSuccess() {
        return status == RankingEventStatus.SUCCESS;
    }
}
