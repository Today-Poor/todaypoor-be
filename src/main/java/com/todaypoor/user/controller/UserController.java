package com.todaypoor.user.controller;

import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.security.CustomUserDetails;
import com.todaypoor.user.dto.UserMeResponse;
import com.todaypoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
