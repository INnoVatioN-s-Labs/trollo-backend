package com.toyproject.trollo.dto.kanban;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "칸반 티켓 응답 DTO")
public record KanbanTicketResponse(
        @Schema(description = "티켓 ID", example = "1001")
        Long id,

        @Schema(description = "티켓 제목", example = "API 스펙 정리")
        String title,

        @Schema(description = "티켓 설명", example = "요청/응답 필드를 확정합니다.")
        String description,

        @Schema(description = "보드 내 위치", example = "2")
        int position
) {
}
