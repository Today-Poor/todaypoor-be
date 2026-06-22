package com.todaypoor.crew.dto.crew.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.openapitools.jackson.nullable.JsonNullable;

import com.todaypoor.crew.entity.AiMode;

public record UpdateCrewRequest(

        String name,

        JsonNullable<String> description,

        @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
        @Max(value = 5, message = "최대 인원은 5명 이하여야 합니다.")
        Integer maxMemberCount,

        AiMode aiMode

) {

    public UpdateCrewRequest {
        description = description == null ? JsonNullable.undefined() : description;
        // JsonNullable.undefined() -> 필드 자체가 미전송
    }
}
