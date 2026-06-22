package com.todaypoor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "내 정보 조회 및 프로필 응답 DTO")
@Getter
public class UserMeResponse {

    @Schema(description = "사용자 서비스 내부 식별 고유 ID (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID userId;

    @Schema(description = "사용자 닉네임 (소셜 닉네임 또는 변경된 닉네임)", example = "뽀로로")
    private final String nickname;

    @Builder
    public UserMeResponse(UUID userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
