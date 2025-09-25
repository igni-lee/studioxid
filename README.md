# Protopie Assignment - 사용자 관리 시스템

백엔드 엔지니어 과제로 구현한 RESTful API 서버입니다.

## 🛠 기술 스택

### Backend
- **언어**: Kotlin
- **프레임워크**: Spring Boot
- **빌드 도구**: Gradle
- **Java 버전**: OpenJDK 21

### Database & Cache
- **주 데이터베이스**: PostgreSQL 15
- **캐시**: Redis 7
- **ORM**: Spring Data JPA + QueryDSL

### Message Queue & Async
- **메시지 큐**: RabbitMQ 3.13
- **비동기 처리**: Spring AMQP

### Security & Auth
- **인증**: JWT (JSON Web Token)
- **암호화**: BCrypt with Salt
- **인가**: Role-Based Access Control (RBAC)

### Documentation & Testing
- **API 문서**: Swagger/OpenAPI 3.0
- **테스트**: JUnit 5, MockMvc, TestContainers
- **코드 품질**: KtLint, JaCoCo

### DevOps & Infrastructure
- **컨테이너화**: Docker & Docker Compose


## 🏗 시스템 아키텍처

### 계층형 아키텍처 (Layered Architecture)

```
┌─────────────────────────────────────────┐
│              Presentation Layer         │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Controller  │  │  Global Exception│   │
│  │             │  │     Handler     │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│              Business Layer             │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │   Service   │  │   Security      │   │
│  │             │  │   (JWT, RBAC)   │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│              Data Access Layer          │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Repository  │  │   Cache Layer   │   │
│  │ (JPA/QueryDSL)│  │    (Redis)     │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│              Infrastructure Layer       │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ PostgreSQL  │  │   RabbitMQ      │   │
│  │             │  │                 │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### 설계 원칙

#### 1. 단일 책임 원칙 (SRP)
- 각 클래스는 하나의 책임만 가짐
- Controller: HTTP 요청/응답 처리
- Service: 비즈니스 로직 처리
- Repository: 데이터 접근 처리

#### 2. 의존성 역전 원칙 (DIP)
- 고수준 모듈이 저수준 모듈에 의존하지 않음
- 인터페이스를 통한 느슨한 결합

#### 3. 개방-폐쇄 원칙 (OCP)
- 확장에는 열려있고 수정에는 닫혀있음
- 새로운 기능 추가 시 기존 코드 수정 최소화

## 🚀 주요 기능

### 1. 인증/인가 시스템

#### JWT 기반 인증
- **토큰 생성**: 사용자 ID, 이메일, 역할 정보 포함
- **토큰 검증**: 서명 검증 및 만료 시간 확인
- **토큰 추출**: HTTP 헤더에서 Bearer 토큰 파싱

#### 역할 기반 접근 제어 (RBAC)
- **ADMIN**: 모든 사용자 관리 권한
- **MEMBER**: 본인 정보만 관리 가능
- **권한 검증**: 메서드 레벨 보안 적용

#### 비밀번호 보안
- **BCrypt 해싱**: 안전한 비밀번호 저장
- **Salt 추가**: 레인보우 테이블 공격 방지
- **정책 검증**: 최소 8자, 영문/숫자/특수문자 포함

### 2. 사용자 관리

#### CRUD 기능
- **생성**: 회원가입 (이메일 중복 검증)
- **조회**: 본인 정보 또는 Admin 전체 조회
- **수정**: 이름, 이메일 변경 (본인/Admin)
- **삭제**: 소프트 삭제 (논리적 삭제)

#### 검색 및 페이징
- **동적 검색**: QueryDSL 기반 조건부 검색
- **페이징**: Spring Data 페이징 지원
- **정렬**: 다양한 필드별 정렬 옵션

### 3. 비동기 처리

#### 메시지 큐 기반 작업
- **사용자 삭제**: 백그라운드에서 데이터 정리
- **캐시 무효화**: Redis 캐시 자동 정리
- **데이터 익명화**: 개인정보 보호 처리

#### RabbitMQ 활용
- **메시지 발행**: 비동기 작업 요청
- **메시지 소비**: 백그라운드 작업 처리
- **재시도 메커니즘**: 실패 시 자동 재시도

### 4. 성능 최적화

#### Redis 캐싱
- **사용자 정보 캐싱**: 자주 조회되는 데이터 캐시
- **TTL 설정**: 자동 만료로 메모리 효율성
- **캐시 무효화**: 데이터 변경 시 캐시 갱신

#### 데이터베이스 최적화
- **인덱스 최적화**: 조회 성능 향상
- **QueryDSL**: 동적 쿼리로 효율적 데이터 접근
- **연결 풀**: HikariCP로 연결 관리

## 📚 API 문서

### 기본 정보
- **Base URL**: `http://localhost:8080`
- **API 버전**: v1
- **인증 방식**: JWT Bearer Token
- **응답 형식**: JSON

### 인증 엔드포인트

#### 회원가입
```http
POST /users/signup
Content-Type: application/json

{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "SecurePass123!",
  "role": "MEMBER"
}
```

**응답 (201 Created)**
```json
{
  "message": "회원가입이 완료되었습니다"
}
```

#### 로그인
```http
POST /users/signin
Content-Type: application/json

{
  "email": "hong@example.com",
  "password": "SecurePass123!"
}
```

**응답 (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

### 사용자 관리 엔드포인트

#### 사용자 조회
```http
GET /users/{userId}
Authorization: Bearer {jwt_token}
```

**응답 (200 OK)**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "role": "MEMBER",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

#### 사용자 수정
```http
PUT /users/{userId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "홍길순",
  "email": "hong2@example.com"
}
```

#### 사용자 삭제
```http
DELETE /users/{userId}
Authorization: Bearer {jwt_token}
```

#### 전체 사용자 조회 (Admin만)
```http
GET /users?page=0&size=10&sort=createdAt&direction=desc
Authorization: Bearer {jwt_token}
```

**응답 (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "name": "홍길동",
      "email": "hong@example.com",
      "role": "MEMBER",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 에러 응답 형식

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2024-01-01T00:00:00",
  "path": "/api/endpoint"
}
```

### HTTP 상태 코드

| 코드 | 의미 | 설명 |
|------|------|------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 중복 데이터 |
| 500 | Internal Server Error | 서버 오류 |

## 🚀 설치 및 실행

### 1. 실행 방법

#### 전체 환경 실행 (권장)
```bash
# 모든 서비스 시작
./scripts/start.sh

# 서비스 중지
./scripts/stop.sh
```

#### 개발 환경 실행
```bash
# 인프라만 실행
./scripts/start-dev.sh

# 애플리케이션 실행
./gradlew bootRun
```

#### 수동 실행
```bash
# 1. PostgreSQL 시작
docker run -d \
  --name postgres \
  -e POSTGRES_DB=protopie_db \
  -e POSTGRES_USER=protopie_user \
  -e POSTGRES_PASSWORD=protopie_pass \
  -p 5432:5432 \
  postgres:15

# 2. Redis 시작
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7

# 3. RabbitMQ 시작
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3-management

# 4. 애플리케이션 실행
./gradlew bootRun
```

## 🐳 Docker 환경

### 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| Spring Boot App | 8080 | 메인 애플리케이션 |
| PostgreSQL | 5432 | 데이터베이스 |
| Redis | 6379 | 캐시 서버 |
| RabbitMQ | 5672 | 메시지 큐 |
| RabbitMQ Management | 15672 | 관리 UI |

### 접속 정보

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서**: http://localhost:8080/api-docs
- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)
- **PostgreSQL**: localhost:5432 (protopie_user/protopie_pass)
- **Redis**: localhost:6379

### 기본 계정

| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| 관리자 | admin@protopie.com | admin123! |
| 일반 사용자 | user@protopie.com | user123! |

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests UserControllerTest

# 테스트 커버리지 생성
./gradlew jacocoTestReport

# 테스트 리포트 확인
open build/reports/tests/test/index.html
open build/reports/jacoco/test/html/index.html
```

### 테스트 구조

```
src/test/kotlin/studioxid/protopieassignment/
├── controller/
│   └── UserControllerTest.kt          # API 통합 테스트
├── service/
│   └── UserServiceTest.kt             # 비즈니스 로직 테스트
├── exception/
│   └── GlobalExceptionHandlerTest.kt  # 예외 처리 테스트
└── TestContainersConfiguration.kt     # 테스트 컨테이너 설정
```

### 테스트 시나리오

#### 1. 인증/인가 테스트
- ✅ 회원가입 성공/실패 케이스
- ✅ 로그인 성공/실패 케이스
- ✅ JWT 토큰 검증
- ✅ 역할 기반 접근 제어

#### 2. 사용자 관리 테스트
- ✅ 사용자 CRUD 작업
- ✅ 권한별 접근 제어
- ✅ 입력값 검증
- ✅ 에러 처리

#### 3. 통합 테스트
- ✅ 데이터베이스 연동
- ✅ 캐시 동작
- ✅ 비동기 처리
- ✅ 메시지 큐 연동
