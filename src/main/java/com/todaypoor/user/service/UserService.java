package com.todaypoor.user.service;

import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.user.dto.UserMeResponse;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자의 ID를 통해 핵심 프로필(식별자 및 닉네임) 정보를 반환합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @return 내 정보 응답 DTO
     */
    @Transactional(readOnly = true)
    public UserMeResponse getMyInfo(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserMeResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
