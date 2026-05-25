package com.todaypoor.crew.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.todaypoor.crew.entity.AiMode;

public record CreateCrewRequest(

        @NotBlank(message = "크루 이름은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "AI 모드는 필수입니다.")
        AiMode aiMode

) {

}
