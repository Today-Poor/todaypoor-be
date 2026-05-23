package com.todaypoor.global.exception;

import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.response.ValidationErrorData;
import com.todaypoor.global.response.ValidationErrorDetail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return buildErrorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ValidationErrorData>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        for (ObjectError objectError : exception.getBindingResult().getGlobalErrors()) {
            errors.add(new ValidationErrorDetail(objectError.getObjectName(), objectError.getDefaultMessage()));
        }

        return buildInvalidRequestResponse(errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ValidationErrorData>> handleBindException(BindException exception) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        for (ObjectError objectError : exception.getBindingResult().getGlobalErrors()) {
            errors.add(new ValidationErrorDetail(objectError.getObjectName(), objectError.getDefaultMessage()));
        }

        return buildInvalidRequestResponse(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ValidationErrorData>> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.add(new ValidationErrorDetail(extractFieldName(violation), violation.getMessage()));
        }

        return buildInvalidRequestResponse(errors);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(Exception exception) {
        return buildErrorResponse(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return buildErrorResponse(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return buildErrorResponse(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    private ResponseEntity<ApiResponse<ValidationErrorData>> buildInvalidRequestResponse(
            List<ValidationErrorDetail> errors
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ValidationErrorData data = new ValidationErrorData(errors);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage(), data));
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String field = "request";

        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getName() != null) {
                field = node.getName();
            }
        }

        return field;
    }
}
