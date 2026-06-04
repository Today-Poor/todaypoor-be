package com.todaypoor.expense.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // 크루 멤버의 가장 최근 소비 내역 조회
    Optional<Expense> findFirstByCrewIdAndUserIdAndDeletedAtIsNullOrderBySpentAtDesc(
            UUID crewId,
            UUID userId
    );
}
