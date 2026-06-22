package com.todaypoor.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.user.entity.SocialAccount;
import com.todaypoor.user.entity.SocialProvider;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

}
