package com.todaypoor.ranking.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "prompt_payload", columnDefinition = "TEXT")
    private String promptPayload;

    @Lob
    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    public static AiRankingRun create(UUID dailyRankingEventId, String promptPayload, String responsePayload) {
        if (dailyRankingEventId == null) throw new IllegalArgumentException("dailyRankingEventId는 필수입니다.");

        AiRankingRun run = new AiRankingRun();
        run.dailyRankingEventId = dailyRankingEventId;
        run.promptPayload = promptPayload;
        run.responsePayload = responsePayload;
        return run;
    }
}