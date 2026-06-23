package com.todaypoor.ranking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;

public interface AiRankingRunRepository extends JpaRepository<AiRankingRun, UUID> {

    Optional<AiRankingRun> findTopByDailyRankingEventIdAndStatusOrderByCreatedAtDesc(
            UUID dailyRankingEventId, AiRankingRunStatus status
    );

    // 내부 API용: 이벤트의 최신 실행 기록 조회 (상태 무관)
    Optional<AiRankingRun> findTopByDailyRankingEventIdOrderByCreatedAtDesc(UUID dailyRankingEventId);
}
