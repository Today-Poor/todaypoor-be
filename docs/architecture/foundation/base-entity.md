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
    - 엔티티 레벨에 `@SQLRestriction("deleted_at IS NULL")`이 적용되어 있어, 일반적인 JPA 조회(findAll, findById 등) 시 자동으로 삭제된 데이터가 제외됩니다.
4. 상태 변경(수정, 삭제 등) 후에는 **`repository.save(entity)`를 명시적으로 호출**하는 것을 원칙으로 한다.
    -Dirty Checking을 통한 자동 반영도 가능하지만, 코드의 명확성과 일관성을 위해 명시적 저장을 권장합니다.
5. 복구가 필요한 경우에만 `restore()`를 호출한다.

## 삭제된 데이터 조회하기
`@SQLRestriction`이 걸려 있으면 일반적인 Spring Data JPA 메서드로는 삭제된 데이터를 찾을 수 없습니다. 재가입이나 데이터 복구 로직 등에서 삭제된 데이터가 필요한 경우 아래 방식을 사용합니다.

1. **Native Query 사용**: `@Query(value = "...", nativeQuery = true)`를 사용하여 직접 SQL을 작성합니다.
2. **별도 Repository 메서드**: `deleted_at`이 포함된 조건을 명시적으로 쿼리하는 메서드를 정의합니다.

예시 (CrewMemberRepository):
```java
@Query(value = "SELECT * FROM crew_member m WHERE m.crew_id = :crewId AND m.user_id = :userId AND m.deleted_at IS NOT NULL", nativeQuery = true)
Optional<CrewMember> findDeletedMember(UUID crewId, UUID userId);
```

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
