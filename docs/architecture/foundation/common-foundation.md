# 공통 기반 사용 가이드

> 이 문서를 보면: 공통 모듈이 제공하는 응답 포맷, 예외 처리, validation 실패 처리, security 401/403 포맷 통일까지 한 번에 파악할 수 있습니다.  
> 언제 다시 보나요: 새 도메인 구현 시작할 때, 예외 코드 추가할 때, 컨트롤러 응답 형식을 맞출 때.

---

## 한 줄 요약
모든 컨트롤러 응답은 `ApiResponse`, 도메인 예외는 `BusinessException + ErrorCode`, validation 실패는 `INVALID_REQUEST + data.errors` 규칙으로 처리합니다.

---

## 목차
1. [패키지 구조 복습](#1-패키지-구조-복습)
2. [BaseEntity 규칙](#2-baseentity-규칙)
3. [응답 포맷 작성](#3-응답-포맷-작성)
4. [예외 던지는 방법](#4-예외-던지는-방법)
5. [검증 실패 처리 규칙](#5-검증-실패-처리-규칙)
6. [Security 401/403 포맷 통일](#6-security-401403-포맷-통일)
7. [테스트 작성 기준](#7-테스트-작성-기준)
8. [실전 예시](#8-실전-예시)

---

## 1. 패키지 구조 복습

`src/main/java/com/todaypoor/global`
- `entity/BaseEntity.java`
- `response/ApiResponse.java`
- `response/ValidationErrorData.java`
- `response/ValidationErrorDetail.java`
- `exception/ErrorCode.java`
- `exception/BusinessException.java`
- `exception/GlobalExceptionHandler.java`

핵심 책임
- `BaseEntity`: 생성/수정/삭제 시각 공통 관리
- `ApiResponse`: 모든 API 반환 포맷 표준화
- `ErrorCode`: 상태코드/코드/메시지 단일 소스
- `BusinessException`: 도메인 예외 전달 객체
- `GlobalExceptionHandler`: 예외 -> JSON 응답 매핑

---

## 2. BaseEntity 규칙

모든 엔티티는 `BaseEntity`를 상속하고 아래 필드를 공통으로 사용합니다.
- `created_at`
- `updated_at`
- `deleted_at`

자세한 규칙은 [BaseEntity 가이드](./base-entity.md)를 따릅니다.

---

## 3. 응답 포맷 작성

### 성공
```json
{
  "success": true,
  "code": "OK",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

### 일반 실패
```json
{
  "success": false,
  "code": "CREW_NOT_FOUND",
  "message": "크루를 찾을 수 없습니다.",
  "data": null
}
```

### 유효성 실패
```json
{
  "success": false,
  "code": "INVALID_REQUEST",
  "message": "잘못된 요청입니다.",
  "data": {
    "errors": [
      { "field": "amount", "message": "금액은 0보다 커야 합니다." },
      { "field": "merchant", "message": "가맹점명은 필수입니다." }
    ]
  }
}
```

---

## 4. 예외 던지는 방법

서비스/도메인 로직에서:
```java
if (crew == null) {
    throw new BusinessException(ErrorCode.CREW_NOT_FOUND);
}
```

규칙
- 컨트롤러에서 문자열/코드를 직접 만들지 않는다.
- 무조건 `ErrorCode`를 통해서 던진다.

---

## 5. 검증 실패 처리 규칙

아래 예외는 전부 `INVALID_REQUEST(400)`으로 통일:
- `MethodArgumentNotValidException`
- `BindException`
- `ConstraintViolationException`
- `MissingServletRequestParameterException`
- `MethodArgumentTypeMismatchException`
- `HttpMessageNotReadableException`

차이점
- 일반 실패: `data = null`
- 검증 실패: `data.errors[]` 채움

---

## 6. Security 401/403 포맷 통일

`@RestControllerAdvice`는 필터 이전 예외를 못 잡기 때문에 Security 핸들러를 별도 구현합니다.

현재 구현 클래스
- `global/config/SecurityConfig`
- `global/security/CustomAuthenticationEntryPoint`
- `global/security/CustomAccessDeniedHandler`

1. `AuthenticationEntryPoint` 구현
   - 인증 실패 시 `UNAUTHORIZED(401)` + `ApiResponse.fail(...)`
2. `AccessDeniedHandler` 구현
   - 권한 부족 시 `FORBIDDEN(403)` + `ApiResponse.fail(...)`
3. `SecurityFilterChain`에 등록
   - `http.exceptionHandling(ex -> ex.authenticationEntryPoint(...).accessDeniedHandler(...))`

---

## 7. 테스트 작성 기준

현재 테스트 파일:
- `src/test/java/com/todaypoor/global/exception/GlobalExceptionHandlerTest.java`

검증 항목
- 비즈니스 예외 -> status/code/message/data
- `@Valid` 실패 -> `INVALID_REQUEST + data.errors`
- 타입 불일치 -> `INVALID_REQUEST`

---

## 8. 실전 예시

컨트롤러:
```java
@PostMapping("/expenses")
public ApiResponse<ExpenseCreateResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
    ExpenseCreateResponse response = expenseService.create(request);
    return ApiResponse.success(response);
}
```

서비스:
```java
public ExpenseCreateResponse create(ExpenseCreateRequest request) {
    User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    // ...
}
```
