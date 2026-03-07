# Trollo Backend (Trello Clone)

Trollo 프로젝트의 백엔드 시스템입니다. Spring Boot를 기반으로 하며, 협업 관리 도구인 Trello의 핵심 기능을 구현하는 것을 목표로 합니다.

## 🚀 프로젝트 개요 (Project Overview)

-   **목적**: 협업 및 작업 관리 툴(Trello Clone)의 백엔드 기능 제공.
-   **핵심 기능**: 워크스페이스(Workspace), 보드(Board), 티켓(Ticket), 사용자(User) 관리.
-   **아키텍처**: Layered Architecture를 따르며, 명확한 책임 분리를 목표로 합니다.
    -   `Controller` -> `Service` -> `Repository` -> `Entity`
    -   데이터 전달에는 전용 `DTO`를 사용합니다.

## 🛠️ 기술 스택 (Tech Stack)

-   **언어**: Java 17
-   **프레임워크**: Spring Boot 3.5.11
-   **데이터베이스**: H2 (개발/테스트용), MySQL (운영용 예정)
-   **ORM**: Spring Data JPA
-   **보안**: Spring Security & JWT (Json Web Token)
-   **문서화**: Swagger/OpenAPI (SpringDoc)
-   **빌드 도구**: Gradle

## 🏗️ 프로젝트 구조 (Project Structure)

-   `com.toyproject.trollo`: 메인 어플리케이션 패키지.
-   `controller`: API 엔드포인트 정의 및 요청 유효성 검증.
-   `service`: 비즈니스 로직(Business Logic) 및 트랜잭션 관리.
-   `entity`: JPA 엔티티(Entity) 및 영속성 레이어 모델. `BaseEntity`를 통한 공통 필드 관리.
-   `repository`: 데이터 저장소 접근 (Spring Data JPA).
-   `dto`: 계층 간 데이터 전송을 위한 객체. 도메인별(auth, board, ticket 등) 하위 패키지로 관리.
-   `security`: 인증(Authentication) 및 인가(Authorization) 관련 (JWT 필터, Token Provider).
-   `config`: 보안, JPA, Swagger 등 시스템 설정(Configuration).
-   `common`: 공통 예외 처리(`exception`), 에러 코드(`code`), 유틸리티(`util`) 및 공통 응답 처리.

## 💻 빌드 및 실행 (Building and Running)

### 빌드 (Build)
```bash
./gradlew build
```

### 실행 (Run)
```bash
./gradlew bootRun
```

### 테스트 (Test)
```bash
./gradlew test
```

## 📜 개발 규칙 (Development Conventions)

### 1. 언어 및 명명 규칙
-   **언어**: 모든 응답 메시지, 주석, 기술 문서는 **한국어**로 작성합니다.
-   **용어**: 기술적 용어는 영어를 병기합니다. (예: 의존성(Dependency), 엔티티(Entity))
-   **명명**: 변수명과 메서드명은 `camelCase`를 사용하며, 클래스명은 `PascalCase`를 따릅니다.

### 2. 아키텍처 규칙
-   **DTO 사용**: `Controller`와 `Service` 사이, 그리고 API 응답 시 `Entity`를 직접 노출하지 않고 반드시 `DTO`를 사용합니다.
-   **예외 처리**: `BusinessException`과 `ErrorCode`를 사용하여 일관된 에러 응답을 제공합니다. `GlobalExceptionHandler`에서 이를 통합 관리합니다.
-   **BaseEntity**: 모든 엔티티는 생성/수정 시간을 자동 관리하는 `BaseEntity`를 상속받아야 합니다.

### 3. 보안 규칙
-   **인증**: 모든 보호된 자원은 JWT를 통해 인증합니다.
-   **민감 정보**: API 키, 비밀번호, 시크릿 키 등은 코드에 직접 하드코딩하지 않습니다. 대신 `application.properties`(또는 `.yml`)에서 환경 변수 치환 방식(`${DB_PASSWORD}`, `${JWT_SECRET}` 등)을 사용하여 외부(OS 환경 변수, CI/CD Secrets 등)로부터 주입받습니다.


### 4. 테스트 및 검증 (CRITICAL)
-   **테스트 강제**: 모든 기능 개발/수정 시 JUnit 5 기반 **단위 테스트(Unit Test)**를 반드시 작성합니다.
-   **검증 필수**: 코드 변경 후 반드시 `./gradlew test`를 실행하여 통과를 확인해야 합니다. 실패 상태는 작업 미완료로 간주합니다.
-   **테스트 범위**: 정상 케이스는 물론, 예외 상황(Exception Case)에 대한 테스트도 반드시 포함합니다.
-   **Git 전략**: 브랜치 생성 시 `feature/`, `fix/`, `refactor/` 접두사를 사용합니다.

## 📝 API 문서
-   로컬 실행 시 `http://localhost:8080/swagger-ui/index.html`에서 API 문서를 확인할 수 있습니다.
