package com.todaypoor.crew.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.todaypoor.crew.entity.CrewMember;

public interface CrewMemberRepository extends JpaRepository<CrewMember, UUID> {

    boolean existsByCrewIdAndUserIdAndDeletedAtIsNull(UUID crewId, UUID userId);

    Optional<CrewMember> findByCrewIdAndUserIdAndDeletedAtIsNull(UUID crewId, UUID userId);

    @Query(value = """
    select * from crew_member
    where crew_id = :crewId and user_id = :userId and deleted_at is not null
    limit 1
    """, nativeQuery = true)
    Optional<CrewMember> findDeletedMember(@Param("crewId") UUID crewId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    update crew_member
    set deleted_at = null, joined_at = :joinedAt, role = :role
    where crew_id = :crewId and user_id = :userId and deleted_at is not null
    """, nativeQuery = true)
    int restoreMember(@Param("crewId") UUID crewId, @Param("userId") UUID userId, @Param("joinedAt") LocalDateTime joinedAt, @Param("role") String role);

    Integer countByCrewIdAndDeletedAtIsNull(UUID crewId);
}
