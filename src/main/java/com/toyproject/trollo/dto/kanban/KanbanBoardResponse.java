package com.toyproject.trollo.dto.kanban;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "칸반 보드 응답 DTO")
public record KanbanBoardResponse(
        @Schema(description = "보드 ID", example = "101")
        Long id,

        @Schema(description = "보드 이름", example = "Todo")
        String name,

        @Schema(description = "보드 위치", example = "1")
        int position,

        @Schema(description = "보드 내 티켓 목록")
        List<KanbanTicketResponse> tickets
) {
}
