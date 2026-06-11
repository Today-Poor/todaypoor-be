package com.todaypoor.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

}
