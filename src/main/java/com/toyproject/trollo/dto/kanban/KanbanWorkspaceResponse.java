package com.toyproject.trollo.dto.kanban;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "칸반 통합 조회 응답 DTO")
public record KanbanWorkspaceResponse(
        @Schema(description = "워크스페이스 ID", example = "10")
        Long workspaceId,

        @Schema(description = "워크스페이스 이름", example = "백엔드")
        String workspaceName,

        @Schema(description = "워크스페이스 설명", example = "백엔드 작업 공간")
        String workspaceDescription,

        @Schema(description = "보드 목록")
        List<KanbanBoardResponse> boards
) {
}
