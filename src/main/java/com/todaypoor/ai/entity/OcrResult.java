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

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Lob
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "merchant_name")
    private String merchantName;

    private Integer amount;

    public static OcrResult create(String imageUrl, String rawJson, String merchantName, Integer amount) {
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("imageUrl은 필수입니다.");

        OcrResult result = new OcrResult();
        result.imageUrl = imageUrl;
        result.rawJson = rawJson;
        result.merchantName = merchantName;
        result.amount = amount;
        return result;
    }
}