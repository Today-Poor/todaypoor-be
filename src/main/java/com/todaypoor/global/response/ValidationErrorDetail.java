package com.todaypoor.global.response;

public record ValidationErrorDetail(
        String field,
        String message
) {
}
