package com.todaypoor.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExceptionController {

    @GetMapping("/test/business")
    String businessException() {
        throw new BusinessException(ErrorCode.CREW_NOT_FOUND);
    }

    @PostMapping("/test/validation")
    String validation(@Valid @RequestBody ValidationRequest request) {
        return "ok";
    }

    @GetMapping("/test/type-mismatch")
    String typeMismatch(@RequestParam Integer count) {
        return "count=" + count;
    }

    record ValidationRequest(
            @NotNull(message = "금액은 0보다 커야 합니다.") Integer amount,
            @NotBlank(message = "가맹점명은 필수입니다.") String merchant
    ) {
    }
}
