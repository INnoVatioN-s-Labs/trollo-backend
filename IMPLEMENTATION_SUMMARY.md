# Trollo 백엔드 구현 전체 정리 (처음부터 현재까지)

## 1. 프로젝트 목표

- Trello-lite 형태의 협업 칸반 보드 백엔드 구현
- 인증/인가, 워크스페이스, 보드, 티켓, 칸반 조회, 협업 멤버 관리, 활동 이력까지 포함

## 2. 기반 구성

- Spring Boot 3.5.11, Java 17, Spring Data JPA, Spring Security, JWT
- 공통 응답: `ReturnMessage`
- 공통 예외: `BusinessException`, `ErrorCode`, `GlobalExceptionHandler`
- 공통 엔티티 필드: `BaseEntity(createdAt, updatedAt, version)`

## 3. 인증(Auth) 단계

- 회원가입/로그인 API 구현
- JWT 발급/검증(`JwtTokenProvider`, `JwtAuthenticationFilter`)
- SecurityConfig로 인증 경로와 공개 경로 분리

## 4. 워크스페이스/보드/티켓 기본 기능 단계

### Workspace
- 생성, 단건 조회, 목록 조회, 삭제

### Board
- 생성, 순서 변경(reorder), 삭제
- 위치(position) 갭 정리 및 충돌 방지 로직 반영

### Ticket
- 생성, 조회, 수정, 이동(동일 보드/다른 보드), 삭제
- 보드 내 위치 재정렬 로직 반영

### Kanban
- 워크스페이스 내 보드/티켓 통합 조회 API 제공

## 5. 협업 모델 고도화 단계 (최근 반영)

### 데이터 모델 전환
- `Workspace.owner` 제거
- `Workspace.inviteCode` 추가 (8자리)
- 신규 엔티티 추가
  - `Membership(workspace, user, role, joinedAt)`
  - `ActivityLog(workspace, user, content, type)`
  - enum: `MembershipRole`, `ActivityType`

### 권한 모델 전환
- 기존 owner 비교 방식 -> membership 기반 접근 제어
- 공통 서비스 도입
  - `WorkspaceAccessService` (멤버/호스트 권한 검증)
  - `ActivityLogService` (활동 로그 저장)

### 협업 API 추가
- `POST /api/workspaces/join` (초대코드 참여)
  - 유효 코드 검증, 중복 멤버 검증, 멤버 10명 제한 검증
- `DELETE /api/workspaces/{workspaceId}/members/{userId}` (멤버 강퇴)
  - HOST 권한 필수, 자기 자신 강퇴 금지, HOST 대상 강퇴 금지
- `PATCH /api/workspaces/{workspaceId}/transfer-host` (호스트 양도)
  - 기존 HOST -> MEMBER, 대상 MEMBER -> HOST

## 6. DTO/Repository/유틸 확장

- DTO 추가
  - `JoinWorkspaceRequest/Response`
  - `TransferHostRequest/Response`
- Repository 추가/확장
  - `MembershipRepository`, `ActivityLogRepository`
  - 멤버 수/역할 조회 메서드 확장
- 유틸 추가
  - `InviteCodeGenerator`

## 7. 테스트 현황

- JUnit5 + Mockito 기반 서비스 단위 테스트 유지
- 협업 기능 관련 정상/예외/경계 케이스(멤버 10명 제한 등) 추가
- 전체 테스트 검증
  - `./gradlew test` 통과

## 8. 문서 반영

- `AGENTS.md`, `GEMINI.md`에 최신 협업/권한/활동이력 규칙 반영 완료
