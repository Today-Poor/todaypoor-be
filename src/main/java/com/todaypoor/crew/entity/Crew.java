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

}
