package com.todaypoor.ranking.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(name = "ai_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class AiResult extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "daily_ranking_event_id", nullable = false)
    private UUID dailyRankingEventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "roast_message", nullable = false, length = 500)
    private String roastMessage;

    public static AiResult create(UUID dailyRankingEventId, UUID userId, String roastMessage) {
        if (dailyRankingEventId == null) throw new IllegalArgumentException("dailyRankingEventId는 필수입니다.");
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (roastMessage == null || roastMessage.isBlank()) throw new IllegalArgumentException("코멘트 메시지는 필수입니다.");

        AiResult result = new AiResult();
        result.dailyRankingEventId = dailyRankingEventId;
        result.userId = userId;
        result.roastMessage = roastMessage;
        return result;
    }
}