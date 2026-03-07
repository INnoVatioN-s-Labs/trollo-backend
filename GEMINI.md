# Trollo Backend (Trello Clone)

Trollo 프로젝트의 백엔드 시스템입니다. Spring Boot를 기반으로 하며, 협업 관리 도구인 Trello의 핵심 기능을 구현하는 것을 목표로 합니다.

## 🚀 프로젝트 개요 (Project Overview)

-   **목적**: 협업 및 작업 관리 툴(Trello Clone)의 백엔드 기능 제공.
-   **핵심 기능**: 워크스페이스(Workspace), 보드(Board), 티켓(Ticket), 사용자(User) 관리.
-   **아키텍처**: Layered Architecture (Controller -> Service -> Repository -> Entity).

## 🛠️ 기술 스택 (Tech Stack)

-   **언어**: Java 17
-   **프레임워크**: Spring Boot 3.5.11
-   **데이터베이스**: H2 (개발/테스트용), MySQL (운영용 예정)
-   **ORM**: Spring Data JPA
-   **보안**: Spring Security
-   **빌드 도구**: Gradle

## 🏗️ 프로젝트 구조 (Project Structure)

-   `com.toyproject.trollo`: 메인 어플리케이션 패키지.
-   `com.toyproject.trollo.entity`: JPA 엔티티(Entity) 정의 (User, Workspace, Board, Ticket).
-   `com.toyproject.trollo.service`: 비즈니스 로직(Business Logic) 구현.
-   `com.toyproject.trollo.config`: 보안(Security) 및 기타 설정(Configuration).

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

-   **언어**: 모든 응답과 주석은 **한국어**로 작성합니다.
-   **용어**: 기술적 용어는 영어를 병기하되 설명은 한국어로 합니다 (예: 엔티티(Entity), 의존성(Dependency)).
-   **명명 규칙**: 변수명과 메서드명은 CamelCase를 따릅니다.
-   **Git 전략**: 브랜치 생성 시 작업 성격에 따라 접두사를 사용합니다 (`feature/`, `fix/`, `refactor/`).
-   **보안**: 민감한 정보(API 키, 비밀번호 등)는 절대 코드에 하드코딩하지 않으며 `.env` 또는 환경 변수를 활용합니다.
