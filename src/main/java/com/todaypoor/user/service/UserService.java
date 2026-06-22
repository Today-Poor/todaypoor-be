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

    /**
     * 사용자의 닉네임을 변경하고 변경된 핵심 프로필 정보를 반환합니다.
     *
     * @param userId 유저 고유 식별자 (UUID)
     * @param request 변경할 정보 (닉네임)
     * @return 변경 완료된 내 정보 DTO
     */
    @Transactional
    public UserMeResponse updateMyInfo(UUID userId, com.todaypoor.user.dto.UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.update(request.getNickname());

        return UserMeResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
