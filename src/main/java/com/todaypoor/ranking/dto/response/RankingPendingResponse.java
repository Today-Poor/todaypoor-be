package com.todaypoor.ranking.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.ranking.entity.RankingEventStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingPendingResponse {

    private UUID crewId;
    private LocalDate rankingDate;
    private RankingEventStatus status;
    private String expectedOpenTime;

    private static final String RANKING_OPEN_TIME = "23:00";

    public static RankingPendingResponse of(UUID crewId, LocalDate rankingDate) {
        return new RankingPendingResponse(
                crewId, rankingDate, RankingEventStatus.PENDING, RANKING_OPEN_TIME
        );
    }
}
