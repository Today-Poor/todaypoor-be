package com.todaypoor.ai.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(name = "ocr_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class OcrResult extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @Column(name = "raw_text", columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(name = "extracted_merchant")
    private String extractedMerchant;

    @Column(name = "extracted_amount")
    private Integer extractedAmount;

    public static OcrResult create(String rawText, String extractedMerchant, Integer extractedAmount) {
        validateCreate(rawText);

        OcrResult result = new OcrResult();
        result.rawText = rawText;
        result.extractedMerchant = extractedMerchant;
        result.extractedAmount = extractedAmount;
        return result;
    }

    private static void validateCreate(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("rawText는 필수입니다.");
        }
    }
}