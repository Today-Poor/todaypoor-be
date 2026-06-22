package com.todaypoor.user.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.todaypoor.global.config.JpaAuditingConfig;
import com.todaypoor.user.entity.User;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Soft Delete된 사용자는 findByIdAndDeletedAtIsNull로 조회되지 않아야 한다")
    void soft_delete_filtering_by_method() {
        // given
        User user = User.create("user1");
        userRepository.save(user);
        
        user.softDelete(); // BaseEntity의 deleted_at 필드를 채움
        userRepository.flush();

        // when
        Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(user.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("@SQLRestriction에 의해 삭제된 사용자는 findAll에서 조회되지 않아야 한다")
    void sql_restriction_filtering() {
        // given
        User user1 = User.create("user1");
        User user2 = User.create("user2");
        userRepository.save(user1);
        userRepository.save(user2);

        user1.softDelete();
        userRepository.flush();

        // when
        var users = userRepository.findAll();

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getNickname()).isEqualTo("user2");
    }
}
