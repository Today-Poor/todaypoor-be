# BaseEntity 가이드

## 목적
- 모든 엔티티에서 `created_at`, `updated_at`, `deleted_at`를 중복 없이 공통 관리한다.
- 생성/수정 시각은 JPA Auditing으로 자동 기록한다.
- 삭제는 물리 삭제 대신 `deleted_at` 기반 소프트 삭제를 기본으로 사용한다.

## 구현 위치
- `src/main/java/com/todaypoor/global/entity/BaseEntity.java`
- `src/main/java/com/todaypoor/global/config/JpaAuditingConfig.java`

## BaseEntity 구성
- `createdAt`: `@CreatedDate`, insert 시 자동 기록
- `updatedAt`: `@LastModifiedDate`, update 시 자동 갱신
- `deletedAt`: null이면 활성 데이터, 값이 있으면 삭제된 데이터

제공 메서드
- `softDelete()`: `deletedAt = now()`
- `restore()`: `deletedAt = null`
- `isDeleted()`: 소프트 삭제 여부 확인

## 사용 규칙
1. 엔티티는 `BaseEntity`를 상속한다.
2. 삭제 API는 `delete()` 대신 `softDelete()` 사용을 기본으로 한다.
3. 조회 시 `deletedAt is null` 조건을 기본 적용한다.
4. 복구가 필요한 경우에만 `restore()`를 호출한다.

## 엔티티 예시
```java
@Entity
public class Crew extends BaseEntity {
    // domain fields
}
```

## 주의사항
- Auditing 값이 들어가려면 Spring Context에서 `@EnableJpaAuditing`이 활성화되어 있어야 한다.
- 테스트에서 Auditing 필드 검증이 필요하면 JPA 저장/수정 후 값을 확인한다.
