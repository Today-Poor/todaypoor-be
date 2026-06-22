package com.todaypoor.user.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("User 엔티티 생성 성공")
    void create_success() {
        // given
        String nickname = "nickname";

        // when
        User user = User.create(nickname);

        // then
        assertThat(user.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("nickname이 null이거나 공백이면 생성 실패")
    void create_fail_invalid_nickname() {
        // when & then
        assertThatThrownBy(() -> User.create(null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> User.create(""))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> User.create("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("User 정보 업데이트 성공")
    void update_success() {
        // given
        User user = User.create("oldName");

        // when
        user.update("newName");

        // then
        assertThat(user.getNickname()).isEqualTo("newName");
    }
}
