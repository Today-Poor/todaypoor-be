package com.todaypoor.user.repository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.todaypoor.global.config.JpaAuditingConfig;
import com.todaypoor.user.entity.SocialAccount;
import com.todaypoor.user.entity.SocialProvider;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class SocialAccountRepositoryTest {

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Test
    @DisplayName("동일한 provider와 providerUserId를 가진 계정은 저장될 수 없다 (유니크 제약)")
    void unique_constraint_fail() {
        // given
        UUID userId1 = UUID.randomUUID();
        SocialAccount account1 = SocialAccount.create(userId1, SocialProvider.KAKAO, "12345");
        socialAccountRepository.save(account1);
        socialAccountRepository.flush();

        // when & then
        UUID userId2 = UUID.randomUUID();
        SocialAccount account2 = SocialAccount.create(userId2, SocialProvider.KAKAO, "12345");
        
        assertThatThrownBy(() -> {
            socialAccountRepository.save(account2);
            socialAccountRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("provider와 providerUserId로 계정 존재 여부를 확인한다")
    void exists_by_provider_and_id() {
        // given
        UUID userId = UUID.randomUUID();
        SocialAccount account = SocialAccount.create(userId, SocialProvider.GOOGLE, "google-id");
        socialAccountRepository.save(account);
        socialAccountRepository.flush();

        // when & then
        assertThat(socialAccountRepository.existsByProviderAndProviderUserId(SocialProvider.GOOGLE, "google-id")).isTrue();
        assertThat(socialAccountRepository.existsByProviderAndProviderUserId(SocialProvider.KAKAO, "google-id")).isFalse();
    }
}
