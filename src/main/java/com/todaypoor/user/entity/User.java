package com.todaypoor.user.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    public static User create(String username, String profileImageUrl) {

        validateUsername(username);

        User user = new User();
        user.username = username;
        user.profileImageUrl = profileImageUrl;

        return user;
    }

    private static void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username은 필수입니다.");
        }
    }

    public void update(String username, String profileImageUrl) {

        if (username != null) {
            validateUsername(username);
            this.username = username;
        }

        this.profileImageUrl = profileImageUrl;
    }

}
