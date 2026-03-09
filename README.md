# Trollo Backend (Trello Clone)

Trollo 프로젝트의 백엔드 서비스 리포지토리입니다. Spring Boot를 기반으로 구현된 RESTful API 서버로, 칸반 보드 형태의 협업 관리 도구에 필요한 핵심 비즈니스 로직과 데이터 검증을 담당합니다.

## 🚀 프로젝트 개요

- **목적**: 효율적인 협업 및 작업 추적을 위한 칸반 보드(Trello Clone) 서비스 API 제공.
- **핵심 도메인**: 사용자(User) 인증, 워크스페이스(Workspace), 보드 리스트(Board), 티켓/카드(Ticket).
- **아키텍처 설계**: 유지보수성과 확장성을 고려한 계층형 아키텍처(Layered Architecture) 적용.
    - 클라이언트 요청 흐름: `Controller` → `Service` → `Repository` → `Entity`
    - 계층 간 데이터 전달 시 엔티티 노출을 막기 위해 전용 `DTO` 객체 활용.

## 🛠️ 기술 스택 (Tech Stack)

- **언어 (Language)**: Java 17
- **프레임워크 (Framework)**: Spring Boot 3.5.11
- **데이터베이스 (Database)**: H2 Database (로컬 개발용), MySQL (운영 환경 적용 예정)
- **ORM**: Spring Data JPA
- **보안 및 인증 (Security)**: Spring Security, JWT (JSON Web Token)
- **API 문서화 (Docs)**: Swagger UI (SpringDoc OpenAPI)
- **빌드 도구 (Build Tool)**: Gradle

## 🏗️ 프로젝트 패키지 구조

```text
src/main/java/com/toyproject/trollo/
├── common/         # 공통 예외 처리(GlobalExceptionHandler), 에러 코드, 유틸리티 모듈
├── config/         # Spring Security, CORS, JPA, Swagger 등 전역 설정(Configuration)
├── controller/     # API 엔드포인트 라우팅 및 클라이언트 요청 DTO 유효성 검증
├── dto/            # 도메인별(auth, board, ticket 등) 계층 이동 데이터 전송 객체
├── entity/         # JPA 엔티티 모델 (공통 필드 관리를 위한 BaseEntity 상속 기본)
├── repository/     # Spring Data JPA 리포지토리 인터페이스
├── security/       # JWT 토큰 발급 및 검증 필터, 인증 컨텍스트 설정 로직
└── service/        # 비즈니스 핵심 로직 처리 및 트랜잭션(@Transactional) 계층
```

## 💻 빌드 및 실행 방법 (Getting Started)

### 1. 요구 사항 (Prerequisites)

- Java 17 이상
- JDK 환경 설정 완료

### 2. 프로젝트 클론 및 빌드

```bash
# 레포지토리 클론 후 이동
cd trollo-backend

# Gradle 기반 빌드 (테스트 제외 시 -x test 옵션 추가)
./gradlew build
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8080` 에서 구동됩니다.

### 4. 테스트 실행

이 프로젝트는 높은 신뢰성을 위해 엄격한 테스트 코드를 유지합니다.

```bash
# 전체 테스트 코드(단위/통합) 실행
./gradlew test
```

## 📜 API 명세서 (Swagger)

로컬 서버 구동 후, 브라우저를 통해 실시간 API 명세서(Swagger UI)를 확인하고 직접 API를 테스트해 볼 수 있습니다.

- **URL**: `http://localhost:8080/swagger-ui/index.html`

## 🔒 보안 및 인증

- 모든 API 응답(일부 예외 제외)은 접근 시 유효한 **JWT (`Bearer Token`)**를 HTTP `Authorization` 헤더에 요구합니다.
- JWT 비밀키(`SECRET_KEY`) 및 환경 설정 값은 소스코드에 하드코딩되지 않으며 환경변수 또는 `.env` 파일로 주입받는 것을 원칙으로 합니다.
