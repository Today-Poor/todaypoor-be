# 💰 거지들의 역습 (TodayPoor) - Backend

거지아워드(Geoji Awards)의 백엔드 서비스입니다. 친구들과 크루(Crew)를 만들어 소비 내역을 공유하고, 영수증 OCR 자동 입력 기능과 Claude AI 기반의 소비 랭킹 및 피드백(Roast/Comfort 모드)을 제공받아 소비를 줄여나가는 재미있는 지출 관리 서비스입니다.

---

## 🚀 주요 기능 (Key Features)

### 1. 인증 및 사용자 관리 (Auth & User)
- **OAuth2 소셜 로그인**: 카카오(Kakao) OAuth2 API를 이용한 간편 로그인
- **JWT 토큰 인증**: Access Token 및 Refresh Token(RTR 구조)을 이용한 무상태(Stateless) 보안인증
- **프로필 관리**: 사용자 정보 조회 및 닉네임 변경 기능

### 2. 크루 관리 (Crew & CrewMember)
- **크루 생성 및 삭제**: 크루 생성 시 정원(최대 5명) 설정 가능, 크루 삭제 시 소속 멤버 정보 일괄 삭제(Cascade Soft-delete) 지원
- **초대코드 시스템**: 고유한 초대코드 발급 및 재발급, 만료 시간(7일) 관리
- **크루 가입 및 권한**: 초대코드를 통한 크루 가입(정원 초과 검증), 방장(Owner)과 일반멤버(Member) 권한 검증 및 방장 위임/강퇴/탈퇴

### 3. 소비/지출 관리 (Expense)
- **지출 내역 등록**: 지출 카테고리(식비, 카페, 쇼핑 등), 금액, 내용, 결제 일시 입력 및 수정/삭제
- **영수증 OCR 자동 파싱**: OCR.space API로 텍스트를 추출한 뒤, Claude API(Haiku)를 이용해 결제 금액 및 카테고리를 자동 파싱하여 등록 지원
- **마스킹 및 프라이버시**: 다른 크루원의 지출 내역 금액 및 내용 노출 옵션 제공

### 4. AI 소비 랭킹 및 코칭 (AI Ranking)
- **일일 랭킹 생성**: 크루 멤버들의 당일 지출 금액을 분석하여 내림차순 랭킹 부여
- **AI 팩트폭행 / 위로 피드백**: 크루가 설정한 AI 모드(ROAST: 팩폭, COMFORT: 위로)에 맞추어 Claude AI 모델이 위트 있는 코칭 피드백 및 뱃지 명칭 생성

---

## 🛠️ 기술 스택 (Tech Stack)

- **언어 및 프레임워크**: Java 17, Spring Boot 4.0.6
- **보안 및 인증**: Spring Security, Spring Security OAuth2 Client, JJWT
- **데이터베이스**: MySQL 8.x, Spring Data JPA, Hibernate, Docker Compose
- **외부 API 연동**:
  - OCR.space API (영수증 이미지 텍스트 추출)
  - Anthropic Claude API (`claude-haiku-4-5-20251001` 모델 연동)
- **API 문서화**: Springdoc OpenAPI (Swagger v3)
- **테스트**: JUnit 5, Mockito, AssertJ

---

## ⚙️ 로컬 개발 환경 설정 (Local Setup)

### 1. 환경 파일 준비
루트 디렉터리에 `.env` 파일을 생성하고 필요한 환경변수 정보를 입력합니다.
```bash
cp .env.example .env
```

**`.env` 예시 설정:**
```properties
JWT_SECRET_KEY=your-jwt-secret-key-must-be-very-long-and-secure
MYSQL_ROOT_PASSWORD=todaypoor
MYSQL_DATABASE=todaypoor
MYSQL_USER=todaypoor
MYSQL_PASSWORD=todaypoor
DB_HOST=localhost
DB_HOST_PORT=3307

# External API Keys (선택)
OCR_SPACE_API_KEY=your-ocr-space-api-key
CLAUDE_API_KEY=your-anthropic-claude-api-key
```

### 2. 로컬 MySQL 실행 (Docker Compose)
MySQL 8.0 컨테이너를 구동합니다. (포트 충돌 방지를 위해 기본 3307 포트로 바인딩하도록 제공됩니다.)
```bash
docker compose up -d
```
자세한 내용은 [로컬 DB 실행 가이드](./docs/operations/local/local-db.md) 문서를 참고하세요.

### 3. 애플리케이션 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 📄 API 명세서 (API Specification)

애플리케이션 실행 후 아래 주소로 접속하면 Swagger UI를 통해 명세 확인 및 API 테스트를 진행할 수 있습니다.
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs 경로**: `http://localhost:8080/v3/api-docs`

---

## 📁 프로젝트 구조 (Package Structure)

```
src/main/java/com/todaypoor/
├── ai/              # OCR 이미지 텍스트 추출 및 Claude AI JSON 파싱 계층
├── auth/            # 소셜 로그인 및 JWT 토큰 처리 계층
├── user/            # 회원 정보 관리 계층
├── crew/            # 크루 생성, 가입, 탈퇴 및 정원/삭제 계층
├── expense/         # 지출 내역 관리 및 영수증 OCR 분석 계층
├── ranking/         # 일일 랭킹 생성 및 Claude AI 코칭 연동 계층
└── global/          # 공통 설정(Security, Exception, Response, Auditing)
```

---

## 📚 관련 개발자 문서 링크 (Developer Guides)
* 💡 [전체 문서 개요 및 인덱스](./docs/overview.md)
* 🚀 [배포 가이드 (CI/CD)](./docs/operations/procedure/deploy.md)
* 🔗 [공통 예외 및 응답 가이드](./docs/architecture/foundation/common-foundation.md)
* 💾 [BaseEntity 및 Auditing 사용법](./docs/architecture/foundation/base-entity.md)
* ⚙️ [로컬 DB 세팅 및 트러블슈팅](./docs/operations/local/local-db.md)
