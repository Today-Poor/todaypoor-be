package com.todaypoor.crew.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.crew.entity.Crew;

public interface CrewRepository extends JpaRepository<Crew, UUID> {

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    Optional<Crew> findByIdAndDeletedAtIsNull(UUID id);

}
