package com.todaypoor.expense.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.todaypoor.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
}