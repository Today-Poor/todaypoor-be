package com.todaypoor.ranking.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(
        name = "ranking_result",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ranking_result_event_id_user_id",
                        columnNames = {"daily_ranking_event_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class RankingResult extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "daily_ranking_event_id", nullable = false)
    private UUID dailyRankingEventId;

    @Column(name = "ai_ranking_run_id", nullable = false)
    private UUID aiRankingRunId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    public static RankingResult create(
            UUID dailyRankingEventId, UUID aiRankingRunId,
            UUID userId, Integer rankNo, Integer totalAmount
    ) {
        validateCreate(dailyRankingEventId, aiRankingRunId, userId, rankNo, totalAmount);

        RankingResult result = new RankingResult();
        result.dailyRankingEventId = dailyRankingEventId;
        result.aiRankingRunId = aiRankingRunId;
        result.userId = userId;
        result.rankNo = rankNo;
        result.totalAmount = totalAmount;
        return result;
    }

    private static void validateCreate(
            UUID dailyRankingEventId, UUID aiRankingRunId,
            UUID userId, Integer rankNo, Integer totalAmount
    ) {
        if (dailyRankingEventId == null) throw new IllegalArgumentException("dailyRankingEventId는 필수입니다.");
        if (aiRankingRunId == null) throw new IllegalArgumentException("aiRankingRunId는 필수입니다.");
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (rankNo == null || rankNo < 1) throw new IllegalArgumentException("올바른 순위(rankNo)가 아닙니다.");
        if (totalAmount == null || totalAmount < 0) throw new IllegalArgumentException("올바른 총 지출액(totalAmount)이 아닙니다.");
    }
}