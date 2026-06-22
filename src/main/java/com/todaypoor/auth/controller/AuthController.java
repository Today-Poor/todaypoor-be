package com.todaypoor.auth.controller;

import com.todaypoor.auth.dto.TokenReissueRequest;
import com.todaypoor.auth.dto.TokenResponse;
import com.todaypoor.auth.service.AuthService;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
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
