package com.todaypoor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "프로필 정보 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Schema(description = "변경하고자 하는 새로운 사용자 닉네임", example = "루피", minLength = 1, maxLength = 20)
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 설정해야 합니다.")
    private String nickname;

    public UserUpdateRequest(String nickname) {
        this.nickname = nickname;
    }
}
