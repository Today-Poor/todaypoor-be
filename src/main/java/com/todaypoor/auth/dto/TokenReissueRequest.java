package com.todaypoor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequest {

    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;

    public TokenReissueRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
