# Berries

아티스트와 팬이 커뮤니티에서 소통하고, 굿즈를 주문하며, 공연 좌석을 예매할 수 있는 팬 커뮤니티 플랫폼의 백엔드 API입니다.

## 프로젝트 개요

Berries는 팬 커뮤니티, 커머스, 공연 예매 기능을 하나의 서비스로 구성한 Spring Boot 프로젝트입니다. JWT 기반 인증과 역할별 권한 제어를 적용했으며, 주문과 좌석 예매처럼 동시 요청에 민감한 기능은 비관적 락과 통합 테스트로 안전성을 검증했습니다.

### 주요 기능

- 이메일 회원가입, 로그인, 토큰 재발급 및 로그아웃
- Access Token과 Refresh Token을 이용한 인증 및 로그아웃 토큰 무효화
- `USER`, `ARTIST`, `MANAGER` 역할 기반 권한 제어
- 아티스트와 담당 멤버·매니저 관리
- 아티스트 팬 커뮤니티 가입 및 탈퇴
- `FAN`, `ARTIST`, `NOTICE` 게시글과 댓글·답글·좋아요
- 아티스트 굿즈 등록·수정, 복수 상품 주문 및 주문 취소
- 공연·좌석 등록, 좌석 예매 및 예매 취소
- 관리자 사용자 검색, 역할·상태 변경
- 담당 아티스트의 기간별 주문·매출 통계
- Swagger/OpenAPI 문서와 일관된 공통 응답·오류 형식

## 핵심 설계

### 동시성 제어

- 좌석 예매 시 대상 좌석에 비관적 쓰기 락을 적용하여 동일 좌석의 동시 예매는 한 건만 성공합니다.
- 상품 주문 시 상품 ID를 정렬한 뒤 비관적 쓰기 락을 획득합니다. 락 획득 순서를 일정하게 유지해 교착 가능성을 줄이고 재고가 음수가 되는 것을 방지합니다.
- 동일 좌석 예매와 한정 재고 주문을 실제 다중 스레드 통합 테스트로 검증합니다.

### 권한과 데이터 보존

- 관리자는 아티스트와 사용자 역할·상태를 관리할 수 있습니다.
- 아티스트 매니저는 자신에게 지정된 아티스트만 관리할 수 있습니다.
- 게시글 등록 유형은 요청에서 입력받지 않고 작성자의 역할에 따라 `USER → FAN`, `ARTIST → ARTIST`, `MANAGER → NOTICE`로 결정합니다.
- 게시글 유형에 따라 작성 권한을 검증합니다.
- 회원 탈퇴 후에도 기존 게시글과 댓글은 보존하며 작성자는 `탈퇴한 사용자`로 표시합니다.
- 답글이 있는 댓글을 삭제해도 트리 구조를 유지하고 본문만 `삭제된 댓글입니다.`로 표시합니다.

### 조회 최적화

- 게시글 목록의 좋아요 수를 페이지 단위로 한 번에 집계해 N+1 쿼리를 방지합니다.
- 관리자 사용자 검색은 역할·상태·키워드 조건을 선택적으로 조합하고 DB 페이징을 적용합니다.
- API 페이지 크기의 최댓값을 100으로 제한해 과도한 조회 요청을 방지합니다.

### 초기 데이터 보존

- `data.sql`은 애플리케이션 시작 시 샘플 데이터를 준비합니다.
- `ON CONFLICT DO NOTHING`을 사용해 이미 존재하는 행을 덮어쓰거나 삭제하지 않습니다.
- 파일 기반 H2 데이터베이스를 사용하므로 애플리케이션 재시작 후에도 로컬 데이터가 유지됩니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Web | Spring Web, Bean Validation |
| Security | Spring Security, JWT (`jjwt` 0.12.6) |
| Persistence | Spring Data JPA, Hibernate, H2 Database |
| Additional | Spring AOP, MyBatis 3.0.4 |
| API Docs | springdoc-openapi 2.8.17, Swagger UI |
| Test | JUnit 5, Mockito, MockMvc, AssertJ |
| Build | Gradle Wrapper |

도메인 저장과 상태 변경은 Spring Data JPA를 사용하고, 채널 기간별 매출 집계는 MyBatis 조회로 분리했습니다. Spring AOP로 모든 REST API의 성공·실패 로그를 공통 처리합니다.

### API 로깅 AOP

`ApiLoggingAspect`는 모든 `@RestController` 실행을 감싸 요청 본문이나 인증정보 없이 다음 항목을 기록합니다.

- 성공: HTTP 메서드, 요청 URI, 실행시간(ms)
- 실패: HTTP 메서드, 요청 URI, 예외 클래스, 예외 메시지
- 실패 예외는 다시 던져 기존 `GlobalExceptionHandler`가 동일하게 처리

```text
API success method=GET uri=/api/products/1 elapsedMs=12
API failure method=POST uri=/api/orders exception=IllegalStateException message=test failure
```

## 프로젝트 구조

```text
src/main/java/com/jjap/berries
├── admin          # 관리자 사용자 조회 및 권한 관리
├── analytics      # 담당 채널 기간별 매출 통계
├── channel        # 채널, 아티스트 멤버, 매니저 관리
├── auth           # 회원가입, 로그인, JWT 재발급·로그아웃
├── community      # 팬 커뮤니티 가입·탈퇴
├── concert        # 공연과 좌석 관리
├── global         # AOP API 로깅, 보안, 공통 응답, 예외, OpenAPI 설정
├── order          # 복수 상품 주문 및 취소
├── post           # 게시글, 댓글, 답글, 좋아요
├── product        # 굿즈 상품과 재고 관리
├── reservation    # 좌석 예매 및 취소
└── user           # 내 정보 조회·수정·탈퇴
```

각 기능 영역은 `controller`, `service`, `repository`, `domain`, `dto` 계층으로 구분합니다.

## 실행 방법

### 1. 요구 환경

- JDK 21
- 별도 데이터베이스 설치는 필요하지 않습니다.
- Windows에서는 포함된 `gradlew.bat`을 사용합니다.

### 2. JWT 비밀키 설정

프로젝트 루트에서 환경변수 예제 파일을 복사합니다.

```powershell
Copy-Item .env.example .env
```

Base64로 인코딩한 32바이트 이상의 키를 생성해 `.env`의 `JWT_SECRET`에 입력합니다.

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

```properties
JWT_SECRET=생성한_Base64_문자열
```

`.env`와 로컬 H2 데이터 파일은 `.gitignore`에 포함되어 저장소에 커밋되지 않습니다.

### 3. 애플리케이션 실행

```powershell
.\gradlew.bat bootRun
```

기본 서버 주소는 `http://localhost:8080`입니다.

### 4. 개발 도구

| 도구 | 주소 |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |
| Health Check | `http://localhost:8080/actuator/health` |

H2 JDBC URL과 사용자 정보는 [`application.yml`](src/main/resources/application.yml)에서 확인할 수 있습니다.

## 인증과 권한

인증 API와 공개 조회 API를 제외한 요청에는 Access Token을 전달해야 합니다.

```http
Authorization: Bearer <access-token>
```

| 역할 | 주요 권한 |
|---|---|
| `USER` | 커뮤니티 가입, FAN 게시글·댓글·좋아요, 주문, 예매 |
| `ARTIST` | 소속 채널의 ARTIST 게시글 작성 |
| `MANAGER` | 담당 채널의 콘텐츠·상품·공연 관리 |

## API 요약

전체 요청·응답 스키마와 오류 응답은 Swagger UI에서 확인할 수 있습니다.

| 영역 | Method | 경로 | 설명 |
|---|---|---|---|
| 인증 | POST | `/api/auth/signup` | 회원가입 |
| 인증 | POST | `/api/auth/login` | 로그인 및 토큰 발급 |
| 인증 | POST | `/api/auth/refresh` | Access Token 재발급 |
| 인증 | POST | `/api/auth/logout` | Access Token 즉시 무효화 및 Refresh Token 폐기 |
| 회원 | GET/PATCH/DELETE | `/api/users/me` | 내 정보 조회·수정·탈퇴 |
| 채널 | GET | `/api/channels`, `/api/channels/{id}` | 채널 목록·상세 조회 |
| 채널 관리 | POST/PATCH | `/api/managers/channels/**` | 담당 매니저의 채널과 담당자 관리 |
| 커뮤니티 | POST/DELETE | `/api/channels/{id}/memberships` | 팬 커뮤니티 가입·탈퇴 |
| 커뮤니티 | GET | `/api/users/me/memberships` | 내 가입 커뮤니티 조회 |
| 게시글 | GET/POST | `/api/channels/{channelId}/posts` | 게시글 목록·작성 |
| 게시글 | GET/PATCH/DELETE | `/api/posts/{id}` | 게시글 상세·수정·삭제 |
| 댓글 | GET/POST | `/api/posts/{id}/comments` | 댓글 목록·작성 |
| 댓글 | POST | `/api/comments/{id}/replies` | 답글 작성 |
| 좋아요 | GET/POST/DELETE | `/api/posts/{id}/likes` | 좋아요 상태·등록·취소 |
| 상품 | GET/POST | `/api/channels/{channelId}/products` | 상품 목록·등록 |
| 상품 | GET/PATCH | `/api/products/{id}` | 상품 상세·수정 |
| 주문 | POST/GET | `/api/orders/**` | 주문 생성·목록·상세 조회 |
| 주문 | POST | `/api/orders/{id}/cancel` | 주문 취소 |
| 공연 | GET/POST | `/api/channels/{channelId}/concerts` | 공연 목록·등록 |
| 좌석 | GET/POST | `/api/concerts/{id}/seats` | 좌석 목록·등록 |
| 좌석 | POST | `/api/concerts/{id}/seats/bulk` | 시작 번호와 개수로 연속 좌석 일괄 등록 |
| 예매 | POST | `/api/concerts/{concertId}/reservations` | 좌석 예매 |
| 예매 | GET | `/api/reservations/**` | 내 예매 목록·상세 조회 |
| 예매 | POST | `/api/reservations/{id}/cancel` | 예매 취소 |
| 매출 통계 | GET | `/api/analytics/{channelId}/sales` | 담당 채널 기간별 매출 조회 |

## 공통 응답

정상 응답과 오류 응답을 공통 형식으로 제공합니다.

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공했습니다."
}
```

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "오류 메시지"
}
```

Validation 실패, 인증·인가 실패, 비즈니스 규칙 위반은 `GlobalExceptionHandler`에서 일관된 HTTP 상태와 오류 코드로 변환합니다.

## 테스트

```powershell
.\gradlew.bat test
```

현재 11개 테스트 클래스, 27개 테스트로 다음 항목을 검증합니다.

- 애플리케이션 컨텍스트 로딩
- JWT 로그인·재발급·로그아웃과 인증 오류
- 회원 조회·수정·탈퇴
- 요청 Validation과 OpenAPI 문서 노출
- 관리자 사용자 검색과 페이징
- 게시글·댓글·답글·좋아요 및 작성 권한
- 상품 주문, 재고 차감과 주문 취소
- 공연 좌석 예매와 예매 취소
- 동일 좌석 및 한정 재고 동시성
- 채널 매출 집계, 취소 주문 제외 및 담당 매니저 권한
- AOP 성공 실행시간 로그와 실패 예외 로그
- 서비스 예외 분기와 도메인 상태 변경

전체 테스트 통과 여부는 Gradle 테스트 리포트 `build/reports/tests/test/index.html`에서도 확인할 수 있습니다.

## 로그 설정

개발 편의를 위해 SQL과 파라미터 바인딩 로그가 기본 활성화되어 있습니다. 운영 환경에서는 민감한 값이 로그에 남지 않도록 다음 환경변수를 설정해야 합니다.

```properties
JPA_SHOW_SQL=false
HIBERNATE_SQL_LOG_LEVEL=INFO
HIBERNATE_BIND_LOG_LEVEL=INFO
```

## 제출 전 확인

```powershell
.\gradlew.bat clean test
```

- `.env`가 Git에 포함되지 않았는지 확인합니다.
- 전체 테스트가 통과하는지 확인합니다.
- Swagger UI에서 주요 API의 요청·응답을 확인합니다.
- 운영·공유 환경에서는 SQL 바인딩 상세 로그를 비활성화합니다.
