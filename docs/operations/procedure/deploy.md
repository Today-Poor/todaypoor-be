# 🚀 배포 가이드 (CI/CD)

이 문서는 **오늘의 거지 (todaypoor-be)** 백엔드 애플리케이션의 배포 프로세스 및 CI/CD 파이프라인 구성에 대해 설명합니다.

---

## 📋 배포 개요
- **CI/CD 플랫폼**: GitHub Actions
- **트리거**: `main` 브랜치로의 `push` 또는 `PR Merge`
- **대상 서버 (Host)**: AWS EC2 인스턴스 (Ubuntu 환경 권장)
- **배포 방식**: Gradle 빌드 후 SCP를 통해 JAR를 전송하고, SSH를 통해 백그라운드(`nohup`)에서 프로세스를 재구동하는 방식

---

## 🔑 GitHub Secrets 설정 (필수)
배포를 정상적으로 작동시키려면 GitHub 저장소의 **Settings > Secrets and variables > Actions**에 아래의 Secret 값들이 반드시 등록되어 있어야 합니다.

| Secret 이름 | 설명 | 예시 / 형태 |
| :--- | :--- | :--- |
| `AWS_HOST` | 배포 대상 EC2 인스턴스의 탄력적 IP (Public IP) | `13.125.xxx.xxx` |
| `AWS_USER` | SSH 접속용 사용자 이름 | `ubuntu` (또는 `ec2-user`) |
| `AWS_SSH_KEY` | SSH 개인키 (`.pem` 파일의 내용 전체) | `-----BEGIN RSA PRIVATE KEY----- ...` |
| `DB_URL` | RDS 혹은 실서비스용 MySQL 데이터베이스 JDBC URL | `jdbc:mysql://{host}:{port}/{db}?serverTimezone=Asia/Seoul` |
| `DB_USERNAME` | 데이터베이스 접속 계정명 | `todaypoor` |
| `DB_PASSWORD` | 데이터베이스 접속 비밀번호 | `********` |
| `JWT_SECRET_KEY` | JWT 서명 및 검증에 사용할 보안 키 | HS256 알고리즘 규격에 맞는 보안 키 |
| `KAKAO_CLIENT_ID` | 카카오 로그인 API Client ID | (REST API 키) |
| `KAKAO_CLIENT_SECRET` | 카카오 로그인 API Client Secret | (카카오 개발자 콘솔에서 발급한 값) |
| `CLAUDE_API_KEY` | 영수증 파싱 및 AI 랭킹 생성용 Claude API Key | `sk-ant-api03-...` |

---

## 🛠️ CI/CD 파이프라인 흐름
GitHub Actions의 배포 워크플로우([deploy.yml](file:///.github/workflows/deploy.yml))는 아래 단계를 거쳐 자동 실행됩니다.

1. **Checkout**: GitHub Runner에 최신 코드를 체크아웃합니다.
2. **JDK 17 설정**: 빌드를 위한 Temurin JDK 17 환경을 구축합니다.
3. **Gradle 빌드**: `./gradlew bootJar` 명령을 실행하여 최적화된 실행형 JAR 파일을 빌드합니다.
4. **파일 전송 (SCP)**: 빌드된 `todaypoor-be-0.0.1-SNAPSHOT.jar` 파일을 대상 EC2의 홈 디렉터리(`/home/{AWS_USER}`)로 전송합니다.
5. **프로세스 재실행 (SSH)**:
   - 실행 중인 기존 Java 프로세스를 종료합니다 (`killall -9 java || true`).
   - GitHub Secrets로부터 주입받은 실서버 환경변수들을 JVM 시스템 프로퍼티(`-D`) 형태로 주입하여 JAR 파일을 백그라운드로 실행합니다.

---

## 💻 EC2 서버 모니터터링 및 트러블슈팅

### 1. 실시간 로그 확인
서버 구동 로그 및 에러 내역은 EC2 서버에 접속한 뒤 아래 명령으로 확인할 수 있습니다.
```bash
# 로그 실시간 추적
tail -f ~/nohup.out
```

### 2. 프로세스 확인
서버가 정상적으로 띄워져서 구동 중인지 포트 및 프로세스를 확인합니다.
```bash
# 프로세스 구동 확인
ps -ef | grep java

# 포트 개방 및 바인딩 상태 확인 (8080 포트)
netstat -lntp | grep 8080
```

### 3. 서버 수동 재구동
배포 스크립트를 거치지 않고 직접 서버를 재구동하고 싶을 때는 아래 명령어를 순서대로 실행합니다.
```bash
# 1. 기존 서버 종료
killall -9 java

# 2. 백그라운드로 서버 재구동 (환경변수가 주입되어 있어야 정상 기동됩니다)
nohup java -DJWT_SECRET_KEY="발급된키" -Dspring.datasource.url="db주소" ... -jar todaypoor-be-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &
```
