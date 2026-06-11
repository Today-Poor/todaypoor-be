package com.todaypoor.user.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_account_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SocialAccount extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, updatable = false)
    private String providerUserId;

    public static SocialAccount create(UUID userId, SocialProvider provider, String providerUserId) {

        validateCreate(userId, provider, providerUserId);

        SocialAccount socialAccount = new SocialAccount();
        socialAccount.userId = userId;
        socialAccount.provider = provider;
        socialAccount.providerUserId = providerUserId;

        return socialAccount;
    }

    private static void validateCreate(UUID userId, SocialProvider provider, String providerUserId) {

        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (provider == null) throw new IllegalArgumentException("provider은 필수입니다.");
        if (providerUserId == null || providerUserId.isBlank()) throw new IllegalArgumentException("providerUserId는 필수입니다.");
    }

}
