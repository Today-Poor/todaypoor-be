package com.todaypoor.expense.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "crew_id", nullable = false)
    private UUID crewId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "spent_at", nullable = false)
    private LocalDateTime spentAt;

    @Column(name = "receipt_image_url")
    private String receiptImageUrl;

    private String memo;

    public static Expense create(UUID crewId, UUID userId, Integer amount, String merchantName, LocalDateTime spentAt, String receiptImageUrl, String memo) {
        validateCreate(crewId, userId, amount, merchantName, spentAt);

        Expense expense = new Expense();
        expense.crewId = crewId;
        expense.userId = userId;
        expense.amount = amount;
        expense.merchantName = merchantName;
        expense.spentAt = spentAt;
        expense.receiptImageUrl = receiptImageUrl;
        expense.memo = memo;
        return expense;
    }

    private static void validateCreate(UUID crewId, UUID userId, Integer amount, String merchantName, LocalDateTime spentAt) {
        if (crewId == null) throw new IllegalArgumentException("crewId는 필수입니다.");
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (amount == null || amount < 0) throw new IllegalArgumentException("올바른 결제 금액이 아닙니다.");
        if (merchantName == null || merchantName.isBlank()) throw new IllegalArgumentException("가맹점명은 필수입니다.");
        if (spentAt == null) throw new IllegalArgumentException("결제 일시는 필수입니다.");
    }
}