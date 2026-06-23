package com.todaypoor.ranking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.ranking.entity.RankingResult;

public interface RankingResultRepository extends JpaRepository<RankingResult, UUID> {

    List<RankingResult> findByDailyRankingEventIdOrderByRankNoAsc(UUID dailyRankingEventId);
}
