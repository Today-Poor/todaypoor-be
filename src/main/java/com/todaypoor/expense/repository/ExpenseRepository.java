package com.todaypoor.expense.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.todaypoor.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByCrewIdAndUserIdAndSpentAtBetweenOrderBySpentAtDesc(
            UUID crewId,
            UUID userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    //혹시 합산을 쓸 수도 있으니까 일단 만들어놓음.
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.crewId = :crewId AND e.userId = :userId AND e.spentAt BETWEEN :start AND :end")
    Optional<Long> sumAmountByCrewIdAndUserIdAndDate(
            @Param("crewId") UUID crewId,
            @Param("userId") UUID userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}