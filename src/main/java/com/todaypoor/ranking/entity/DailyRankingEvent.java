package com.todaypoor.ranking.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "daily_ranking_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class DailyRankingEvent extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "crew_id", nullable = false)
    private UUID crewId;

    @Column(name = "ranking_date", nullable = false)
    private LocalDate rankingDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RankingEventStatus status;

    public static DailyRankingEvent create(UUID crewId, LocalDate rankingDate) {
        if (crewId == null) throw new IllegalArgumentException("crewId는 필수입니다.");
        if (rankingDate == null) throw new IllegalArgumentException("rankingDate는 필수입니다.");

        DailyRankingEvent event = new DailyRankingEvent();
        event.crewId = crewId;
        event.rankingDate = rankingDate;
        event.status = RankingEventStatus.PENDING;
        return event;
    }
}