package com.todaypoor.auth.controller;

import com.todaypoor.auth.dto.TokenReissueRequest;
import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.auth.service.AuthService;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 (Auth)", description = "소셜 로그인 및 JWT 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "토큰 재발급 (Reissue)", description = "만료된 Access Token을 Refresh Token을 통해 갱신합니다. (RTR 패턴 적용)")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @Parameter(description = "Authorization Header (Bearer <RefreshToken>)")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) TokenReissueRequest request
    ) {
        String token = null;

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            token = request.getRefreshToken();
        }

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        TokenResponse response = authService.reissue(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
