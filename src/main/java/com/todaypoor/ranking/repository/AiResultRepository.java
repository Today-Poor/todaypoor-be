package com.todaypoor.ranking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.ranking.entity.AiResult;

public interface AiResultRepository extends JpaRepository<AiResult, UUID> {

    List<AiResult> findByRankingResultIdIn(List<UUID> rankingResultIds);
}
