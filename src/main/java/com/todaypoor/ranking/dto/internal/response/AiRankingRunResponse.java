package com.todaypoor.ranking.dto.internal.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiRankingRunResponse {

    private UUID aiRankingRunId;
    private UUID dailyRankingEventId;
    private AiRankingRunStatus status;
    private String generatedTopic;
    private String rankingCriteria;
    private String model;
    private Integer inputToken;
    private Integer outputToken;
    private String promptVersion;
    private String inputData;
    private String errorCode;
    private LocalDateTime createdAt;

    public static AiRankingRunResponse from(AiRankingRun run) {
        return new AiRankingRunResponse(
                run.getId(),
                run.getDailyRankingEventId(),
                run.getStatus(),
                run.getGeneratedTopic(),
                run.getRankingCriteria(),
                run.getModel(),
                run.getInputToken(),
                run.getOutputToken(),
                run.getPromptVersion(),
                run.getInputData(),
                run.getErrorCode(),
                run.getCreatedAt()
        );
    }
}
