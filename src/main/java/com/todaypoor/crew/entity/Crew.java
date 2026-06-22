package com.todaypoor.crew.entity;

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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(
        name = "crew",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_crew_invite_code", columnNames = "invite_code")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Crew extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "invite_code", nullable = false)
    private String inviteCode;

    @Column(name = "invite_code_expires_at", nullable = false)
    private LocalDateTime inviteCodeExpiresAt;

    @Column(name = "ai_mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private AiMode aiMode;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "max_member_count", nullable = false)
    private Integer maxMemberCount;

    public static Crew create(
            String name,
            String description,
            String inviteCode,
            LocalDateTime inviteCodeExpiresAt,
            AiMode aiMode,
            UUID ownerId,
            Integer maxMemberCount
    ) {
        validateCreate(name, inviteCode, inviteCodeExpiresAt, aiMode, ownerId, maxMemberCount);

        Crew crew = new Crew();
        crew.name = name;
        crew.description = description;
        crew.inviteCode = inviteCode;
        crew.inviteCodeExpiresAt = inviteCodeExpiresAt;
        crew.aiMode = aiMode;
        crew.ownerId = ownerId;
        crew.maxMemberCount = maxMemberCount;
        return crew;
    }

    private static void validateCreate(
            String name, String inviteCode, LocalDateTime inviteCodeExpiresAt, AiMode aiMode, UUID ownerId, Integer maxMemberCount
    ) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name은 필수입니다.");
        validateInviteCode(inviteCode, inviteCodeExpiresAt);
        if (aiMode == null) throw new IllegalArgumentException("aiMode는 필수입니다.");
        if (ownerId == null) throw new IllegalArgumentException("ownerId는 필수입니다.");
        if (maxMemberCount == null) throw new IllegalArgumentException("maxMemberCount는 필수입니다.");

    }

    private static void validateInviteCode(String inviteCode, LocalDateTime inviteCodeExpiresAt) {
        if (inviteCode == null || inviteCode.isBlank()) throw new IllegalArgumentException("inviteCode는 필수입니다.");
        if (inviteCodeExpiresAt == null) throw new IllegalArgumentException("inviteCodeExpiresAt는 필수입니다.");
        if (!inviteCodeExpiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("inviteCodeExpiresAt는 현재 시각 이후여야 합니다.");
        }
    }

    public void update(String name, String description, Integer maxMemberCount, AiMode aiMode) {
        if (name != null) {
            this.name = name;
        }

        this.description = description; // nullable 이므로, update 값이 null이어도 null로 수정 가능

        if (maxMemberCount != null) {
            this.maxMemberCount = maxMemberCount;
        }

        if (aiMode != null) {
            this.aiMode = aiMode;
        }
    }

    public void regenerateInviteCode(String inviteCode, LocalDateTime inviteCodeExpiresAt) {
        validateInviteCode(inviteCode, inviteCodeExpiresAt);

        this.inviteCode = inviteCode;
        this.inviteCodeExpiresAt = inviteCodeExpiresAt;
    }

}
