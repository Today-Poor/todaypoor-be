package com.todaypoor.ranking.dto.internal.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingGenerateResponse {

    private UUID rankingEventId;
    private UUID crewId;
    private LocalDate rankingDate;
    private RankingEventStatus status;

    public static RankingGenerateResponse from(DailyRankingEvent event) {
        return new RankingGenerateResponse(
                event.getId(),
                event.getCrewId(),
                event.getRankingDate(),
                event.getStatus()
        );
    }
}
