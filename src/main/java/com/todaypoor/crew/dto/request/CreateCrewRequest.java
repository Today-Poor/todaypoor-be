package com.todaypoor.crew.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.todaypoor.crew.entity.AiMode;

public record CreateCrewRequest(

        @NotBlank(message = "크루 이름은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "최대 인원은 필수입니다.")
        @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
        @Max(value = 5, message = "최대 인원은 5명 이하여야 합니다.")
        Integer maxMemberCount,

        @NotNull(message = "AI 모드는 필수입니다.")
        AiMode aiMode

) {

}
