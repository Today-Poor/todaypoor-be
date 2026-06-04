package com.todaypoor.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CREW_NOT_FOUND(HttpStatus.NOT_FOUND, "CREW_NOT_FOUND", "크루를 찾을 수 없습니다."),
    CREW_MEMBER_NOT_FOUND(HttpStatus.FORBIDDEN, "CREW_MEMBER_NOT_FOUND", "해당 크루의 멤버가 아닙니다."),
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE_NOT_FOUND", "결제 내역을 찾을 수 없습니다."),
    OCR_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "OCR_RESULT_NOT_FOUND", "OCR 결과를 찾을 수 없습니다."),
    RANKING_NOT_FOUND(HttpStatus.NOT_FOUND, "RANKING_NOT_FOUND", "랭킹 결과를 찾을 수 없습니다."),
    RANKING_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "RANKING_GENERATION_FAILED",
            "랭킹 생성에 실패했습니다."
    ),
    INVALID_MAX_MEMBER_COUNT(
            HttpStatus.BAD_REQUEST,
            "INVALID_MAX_MEMBER_COUNT",
            "크루 최대 인원은 1명 이상 5명 이하로 설정해야 합니다."
    ),
    MAX_MEMBER_COUNT_LESS_THAN_CURRENT(
            HttpStatus.BAD_REQUEST,
            "MAX_MEMBER_COUNT_LESS_THAN_CURRENT",
            "현재 크루원 수보다 작은 최대 인원으로 변경할 수 없습니다."
    ),
    CREW_MEMBER_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "CREW_MEMBER_LIMIT_EXCEEDED",
            "크루 최대 인원을 초과했습니다."
    ),
    OCR_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OCR_FAILED",
            "OCR 분석에 실패했습니다."
    ),
    AI_PARSING_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_PARSING_FAILED",
            "소비내역 파싱에 실패했습니다."
    ),
    EMPTY_PARSED_EXPENSES(
            HttpStatus.BAD_REQUEST,
            "EMPTY_PARSED_EXPENSES",
            "파싱된 소비내역이 없습니다."
    ),
    EMPTY_EXPENSE_LIST(
            HttpStatus.BAD_REQUEST,
            "EMPTY_EXPENSE_LIST",
            "저장할 결제 내역이 없습니다."
    ),
    INVALID_EXPENSE_LIST_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_EXPENSE_LIST_REQUEST",
            "저장할 결제 내역 요청이 올바르지 않습니다."
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "유효하지 않은 refresh token입니다."
    ),
    EXPIRED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "EXPIRED_REFRESH_TOKEN",
            "refresh token이 만료되었습니다. 다시 로그인해주세요."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
