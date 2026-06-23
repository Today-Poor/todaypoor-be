package com.todaypoor.user.service;

import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.user.dto.UserMeResponse;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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
                .orElseThrow(() -> {
                    log.warn("내 정보 조회 실패: 존재하지 않는 유저입니다. 유저 ID: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("내 정보 조회 성공. 유저 ID: {}, 닉네임: {}", userId, user.getNickname());

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
                .orElseThrow(() -> {
                    log.warn("내 정보 수정 실패: 존재하지 않는 유저입니다. 유저 ID: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        String oldNickname = user.getNickname();
        user.update(request.getNickname());

        log.info("내 정보 수정 성공. 유저 ID: {}, 이전 닉네임: {}, 변경된 닉네임: {}", userId, oldNickname, user.getNickname());

        return UserMeResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
