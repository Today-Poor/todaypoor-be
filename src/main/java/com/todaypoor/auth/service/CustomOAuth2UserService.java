package com.todaypoor.auth.service;

import com.todaypoor.global.security.CustomUserDetails;
import com.todaypoor.user.entity.SocialAccount;
import com.todaypoor.user.entity.SocialProvider;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.repository.SocialAccountRepository;
import com.todaypoor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!"kakao".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerUserId = String.valueOf(attributes.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            throw new OAuth2AuthenticationException("카카오 사용자 프로필 정보를 불러올 수 없습니다.");
        }
        String nickname = (String) properties.get("nickname");

        log.info("카카오 OAuth2 로그인 요청 - providerUserId: {}, nickname: {}", providerUserId, nickname);

        User user = socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId)
                .map(socialAccount -> userRepository.findByIdAndDeletedAtIsNull(socialAccount.getUserId())
                        .orElseThrow(() -> new OAuth2AuthenticationException("소셜 계정과 연동된 사용자를 찾을 수 없습니다.")))
                .orElseGet(() -> registerNewUser(providerUserId, nickname));

        return new CustomUserDetails(user, attributes);
    }

    private User registerNewUser(String providerUserId, String nickname) {
        log.info("신규 카카오 소셜 회원가입 진행 - providerUserId: {}, nickname: {}", providerUserId, nickname);

        User newUser = User.create(nickname);
        User savedUser = userRepository.save(newUser);

        SocialAccount socialAccount = SocialAccount.create(savedUser.getId(), SocialProvider.KAKAO, providerUserId);
        socialAccountRepository.save(socialAccount);

        return savedUser;
    }
}
