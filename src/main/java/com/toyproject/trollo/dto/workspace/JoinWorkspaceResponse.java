package com.toyproject.trollo.dto.workspace;

import com.toyproject.trollo.entity.MembershipRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "워크스페이스 참여 응답 DTO")
public record JoinWorkspaceResponse(
        @Schema(description = "워크스페이스 ID", example = "10")
        Long workspaceId,
        @Schema(description = "워크스페이스 이름", example = "백엔드")
        String workspaceName,
        @Schema(description = "멤버 역할", example = "MEMBER")
        MembershipRole role,
        @Schema(description = "참여 시각", example = "2026-03-08T16:20:00")
        LocalDateTime joinedAt
) {
}
