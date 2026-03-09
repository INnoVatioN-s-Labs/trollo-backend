package com.toyproject.trollo.dto.activity;

import com.toyproject.trollo.entity.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "활동 이력 응답 DTO")
public record ActivityLogResponse(
        @Schema(description = "활동 이력 ID", example = "1")
        Long id,
        @Schema(description = "활동 타입", example = "TICKET_MOVE")
        ActivityType type,
        @Schema(description = "활동 내용", example = "티켓을 이동했습니다: API 구현")
        String content,
        @Schema(description = "행위 사용자 ID", example = "2")
        Long userId,
        @Schema(description = "행위 사용자 닉네임", example = "개발자")
        String userNickname,
        @Schema(description = "생성 시각", example = "2026-03-08T16:30:00")
        LocalDateTime createdAt,
        @Schema(description = "워크스페이스 ID", example = "1")
        Long workspaceId,
        @Schema(description = "워크스페이스 이름", example = "백엔드 팀")
        String workspaceName
) {
}
