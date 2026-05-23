# 로컬 DB 실행 가이드 (Docker Compose)

## 목적
- 팀원이 동일한 로컬 MySQL 환경에서 백엔드를 실행할 수 있도록 한다.
- 로컬 포트 충돌(특히 `3306`) 문제를 빠르게 해결한다.

## 범위
- 이번 작업은 **공용 DB 구축 목적이 아님**.
- 공용 DB/EC2 MySQL은 이후 `dev` 인프라 단계에서 진행한다.

## 기본 실행
1. 환경 파일 준비
```bash
cp .env.example .env
```

2. 컨테이너 실행
```bash
docker compose up -d
docker compose ps
```

정상 상태면 `todaypoor-mysql` 컨테이너가 `Up`으로 표시된다.

## 자주 발생하는 오류

### 오류 메시지
`Ports are not available ... bind: address already in use`

### 원인
- 호스트의 `3306` 포트를 이미 다른 프로세스(로컬 MySQL, 다른 컨테이너)가 사용 중이다.

### 해결 방법 (권장: .env에서 포트만 변경)
`.env`에서 포트 변경:
```properties
DB_HOST_PORT=3307
```

재실행:
```bash
docker compose down
docker compose up -d
```

## 종료/정리
```bash
docker compose down
```

데이터 볼륨까지 초기화:
```bash
docker compose down -v
```

## 권장 설정
- 로컬 DB 설정은 `application-local.properties`로 분리하고,
- 실행 시 `--spring.profiles.active=local`을 사용한다.
- `application-local.properties`는 `.env`를 import해서 DB 접속 정보를 사용한다.

예시:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
