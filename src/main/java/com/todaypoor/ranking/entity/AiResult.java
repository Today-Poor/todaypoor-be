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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;
import com.todaypoor.crew.entity.AiMode; // ⚠️ 여원이가 만든 AiMode 패키지 경로에 맞게 확인!

@Entity
@Table(
        name = "ai_result",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_result_ranking_result_id",
                        columnNames = "ranking_result_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class AiResult extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ranking_result_id", nullable = false)
    private UUID rankingResultId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "roast_message", columnDefinition = "TEXT", nullable = false)
    private String roastMessage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiMode mode;

    @Lob
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    private String model;

    @Column(name = "input_token")
    private Integer inputToken;

    @Column(name = "output_token")
    private Integer outputToken;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiResultStatus status;

    @Column(name = "error_code")
    private String errorCode;

    public static AiResult create(
            UUID rankingResultId, String title, String roastMessage,
            AiMode mode, String inputData, String model,
            Integer inputToken, Integer outputToken, String promptVersion,
            AiResultStatus status, String errorCode
    ) {
        validateCreate(rankingResultId, title, roastMessage, mode, status);

        AiResult result = new AiResult();
        result.rankingResultId = rankingResultId;
        result.title = title;
        result.roastMessage = roastMessage;
        result.mode = mode;
        result.inputData = inputData;
        result.model = model;
        result.inputToken = inputToken;
        result.outputToken = outputToken;
        result.promptVersion = promptVersion;
        result.status = status;
        result.errorCode = errorCode;
        return result;
    }

    private static void validateCreate(
            UUID rankingResultId, String title, String roastMessage,
            AiMode mode, AiResultStatus status
    ) {
        if (rankingResultId == null) throw new IllegalArgumentException("rankingResultId는 필수입니다.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title은 필수입니다.");
        if (roastMessage == null || roastMessage.isBlank()) throw new IllegalArgumentException("roastMessage는 필수입니다.");
        if (mode == null) throw new IllegalArgumentException("mode는 필수입니다.");
        if (status == null) throw new IllegalArgumentException("status는 필수입니다.");
    }
}