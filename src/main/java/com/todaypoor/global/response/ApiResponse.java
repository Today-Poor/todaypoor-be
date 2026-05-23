package com.todaypoor.global.response;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    private static final String SUCCESS_CODE = "OK";
    private static final String SUCCESS_MESSAGE = "요청이 성공했습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return fail(code, message, null);
    }
}
