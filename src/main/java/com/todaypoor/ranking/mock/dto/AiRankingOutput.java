package com.todaypoor.ranking.mock.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.crew.entity.AiMode;

@Getter
@AllArgsConstructor
public class AiRankingOutput {

    private String generatedTopic;
    private String rankingCriteria;
    private String model;
    private int inputToken;
    private int outputToken;
    private String promptVersion;
    private String inputData;
    private List<UserRankingItem> userRankings;

    @Getter
    @AllArgsConstructor
    public static class UserRankingItem {

        private UUID userId;
        private int totalAmount;
        private int rankNo;
        private String title;        // 4위 이하는 null
        private String roastMessage; // 4위 이하는 null
        private AiMode mode;         // 4위 이하는 null
    }
}
