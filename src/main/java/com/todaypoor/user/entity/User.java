package com.todaypoor.user.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.global.entity.BaseEntity;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    public static User create(String nickname, String profileImageUrl) {

        validateNickname(nickname);

        User user = new User();
        user.nickname = nickname;
        user.profileImageUrl = profileImageUrl;

        return user;
    }

    private static void validateNickname(String nickname) {

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("username은 필수입니다.");
        }
    }

    public void update(String nickname, String profileImageUrl) {

        if (nickname != null) {
            validateNickname(nickname);
            this.nickname = nickname;
        }

        this.profileImageUrl = profileImageUrl;
    }

}
