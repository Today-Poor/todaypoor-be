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
        name = "crew_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_crew_member_crew_id_user_id",
                        columnNames = {"crew_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class CrewMember extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "crew_id", nullable = false)
    private UUID crewId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CrewRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public static CrewMember createOwner(UUID crewId, UUID userId) {
        return create(crewId, userId, CrewRole.OWNER);
    }

    public static CrewMember createMember(UUID crewId, UUID userId) {
        return create(crewId, userId, CrewRole.MEMBER);
    }

    private static CrewMember create(UUID crewId, UUID userId, CrewRole role) {
        validateCreate(crewId, userId, role);

        CrewMember crewMember = new CrewMember();
        crewMember.crewId = crewId;
        crewMember.userId = userId;
        crewMember.role = role;
        crewMember.joinedAt = LocalDateTime.now();
        return crewMember;
    }

    private static void validateCreate(UUID crewId, UUID userId, CrewRole role) {
        if (crewId == null) throw new IllegalArgumentException("crewId는 필수입니다.");
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");
        if (role == null) throw new IllegalArgumentException("role은 필수입니다.");
    }

    public void restoreMember(CrewRole role) {
        if (role == null) throw new IllegalArgumentException("role은 필수입니다.");
        this.role = role;
        this.joinedAt = LocalDateTime.now();
        this.restore(); // BaseEntity
    }
}
