package com.todaypoor.ranking.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(name = "ai_ranking_run")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class AiRankingRun extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "daily_ranking_event_id", nullable = false)
    private UUID dailyRankingEventId;

    @Lob
    @Column(name = "input_data", columnDefinition = "TEXT", nullable = false)
    private String inputData;

    @Column(name = "generated_topic", nullable = false)
    private String generatedTopic;

    @Lob
    @Column(name = "ranking_criteria", columnDefinition = "TEXT", nullable = false)
    private String rankingCriteria;

    @Column(nullable = false)
    private String model;

    @Column(name = "input_token", nullable = false)
    private Integer inputToken;

    @Column(name = "output_token", nullable = false)
    private Integer outputToken;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiRankingRunStatus status;

    @Column(name = "error_code")
    private String errorCode;

    public static AiRankingRun create(
            UUID dailyRankingEventId, String inputData, String generatedTopic,
            String rankingCriteria, String model, Integer inputToken,
            Integer outputToken, String promptVersion, AiRankingRunStatus status,
            String errorCode
    ) {
        validateCreate(
                dailyRankingEventId, inputData, generatedTopic,
                rankingCriteria, model, inputToken, outputToken,
                promptVersion, status
        );

        AiRankingRun run = new AiRankingRun();
        run.dailyRankingEventId = dailyRankingEventId;
        run.inputData = inputData;
        run.generatedTopic = generatedTopic;
        run.rankingCriteria = rankingCriteria;
        run.model = model;
        run.inputToken = inputToken;
        run.outputToken = outputToken;
        run.promptVersion = promptVersion;
        run.status = status;
        run.errorCode = errorCode;
        return run;
    }

    private static void validateCreate(
            UUID dailyRankingEventId, String inputData, String generatedTopic,
            String rankingCriteria, String model, Integer inputToken,
            Integer outputToken, String promptVersion, AiRankingRunStatus status
    ) {
        if (dailyRankingEventId == null) throw new IllegalArgumentException("dailyRankingEventId는 필수입니다.");
        if (inputData == null || inputData.isBlank()) throw new IllegalArgumentException("inputData는 필수입니다.");
        if (generatedTopic == null || generatedTopic.isBlank()) throw new IllegalArgumentException("generatedTopic은 필수입니다.");
        if (rankingCriteria == null || rankingCriteria.isBlank()) throw new IllegalArgumentException("rankingCriteria는 필수입니다.");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model은 필수입니다.");
        if (inputToken == null || inputToken < 0) throw new IllegalArgumentException("올바른 inputToken 값이 아닙니다.");
        if (outputToken == null || outputToken < 0) throw new IllegalArgumentException("올바른 outputToken 값이 아닙니다.");
        if (promptVersion == null || promptVersion.isBlank()) throw new IllegalArgumentException("promptVersion은 필수입니다.");
        if (status == null) throw new IllegalArgumentException("status는 필수입니다.");
    }
}