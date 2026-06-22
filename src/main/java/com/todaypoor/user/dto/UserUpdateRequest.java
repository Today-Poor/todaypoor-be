package com.todaypoor.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 설정해야 합니다.")
    private String nickname;

    public UserUpdateRequest(String nickname) {
        this.nickname = nickname;
    }
}
