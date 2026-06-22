package com.todaypoor.user.controller;

import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.security.CustomUserDetails;
import com.todaypoor.user.dto.UserMeResponse;
import com.todaypoor.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 (User)", description = "내 정보 조회 및 프로필 설정 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 핵심 정보(UUID 식별자 및 닉네임)를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인된 사용자의 닉네임을 변경하고 수정된 프로필 정보를 반환합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @jakarta.validation.Valid @RequestBody com.todaypoor.user.dto.UserUpdateRequest request
    ) {
        UserMeResponse response = userService.updateMyInfo(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
