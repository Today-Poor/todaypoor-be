package com.todaypoor.expense.entity;

import java.time.LocalDateTime;
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
@Table(name = "expense")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Expense extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "crew_id", nullable = false)
    private UUID crewId;

    @Column(name = "ocr_result_id", nullable = false)
    private UUID ocrResultId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String merchant;

    private String memo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseVisibility visibility;

    @Column(name = "spent_at", nullable = false)
    private LocalDateTime spentAt;

    public static Expense create(
            UUID userId, UUID crewId, UUID ocrResultId,
            ExpenseCategory category, Integer amount,
            String merchant, String memo,
            ExpenseVisibility visibility, LocalDateTime spentAt
    ) {
        validateCreate(userId, crewId, ocrResultId, category, amount, merchant, visibility, spentAt);

        Expense expense = new Expense();
        expense.userId = userId;
        expense.crewId = crewId;
        expense.ocrResultId = ocrResultId;
        expense.category = category;
        expense.amount = amount;
        expense.merchant = merchant;
        expense.memo = memo;
        expense.visibility = visibility;
        expense.spentAt = spentAt;
        return expense;
    }

    private static void validateCreate(
            UUID userId, UUID crewId, UUID ocrResultId,
            ExpenseCategory category, Integer amount,
            String merchant, ExpenseVisibility visibility, LocalDateTime spentAt
    ) {
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (crewId == null) throw new IllegalArgumentException("crewId는 필수입니다.");
        if (ocrResultId == null) throw new IllegalArgumentException("ocrResultId는 필수입니다.");
        if (category == null) throw new IllegalArgumentException("category는 필수입니다.");
        if (amount == null || amount < 0) throw new IllegalArgumentException("올바른 금액이 아닙니다.");
        if (merchant == null || merchant.isBlank()) throw new IllegalArgumentException("merchant는 필수입니다.");
        if (visibility == null) throw new IllegalArgumentException("visibility는 필수입니다.");
        if (spentAt == null) throw new IllegalArgumentException("spentAt은 필수입니다.");
    }

    public void update(
            ExpenseCategory category, Integer amount,
            String merchant, String memo,
            ExpenseVisibility visibility, LocalDateTime spentAt
    ) {
        if (amount == null || amount < 0) throw new IllegalArgumentException("올바른 금액이 아닙니다.");
        if (merchant == null || merchant.isBlank()) throw new IllegalArgumentException("merchant는 필수입니다.");

        this.category = category;
        this.amount = amount;
        this.merchant = merchant;
        this.memo = memo;
        this.visibility = visibility;
        this.spentAt = spentAt;
    }
}