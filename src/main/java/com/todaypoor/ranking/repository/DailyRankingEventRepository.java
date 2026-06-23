package com.todaypoor.ranking.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.ranking.entity.DailyRankingEvent;

public interface DailyRankingEventRepository extends JpaRepository<DailyRankingEvent, UUID> {

    Optional<DailyRankingEvent> findByCrewIdAndRankingDate(UUID crewId, LocalDate rankingDate);
}
