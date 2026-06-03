package com.todaypoor.crew.dto.crew.request;

import jakarta.validation.constraints.NotBlank;

public record JoinCrewRequest(

        @NotBlank(message = "초대 코드는 필수입니다.")
        String inviteCode

) {

}
