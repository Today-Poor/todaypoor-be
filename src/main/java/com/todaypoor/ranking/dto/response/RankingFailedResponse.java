package com.todaypoor.ranking.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.ranking.entity.RankingEventStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingFailedResponse {

    private UUID crewId;
    private LocalDate rankingDate;
    private RankingEventStatus status;

    public static RankingFailedResponse of(UUID crewId, LocalDate rankingDate) {
        return new RankingFailedResponse(crewId, rankingDate, RankingEventStatus.FAILED);
    }
}
