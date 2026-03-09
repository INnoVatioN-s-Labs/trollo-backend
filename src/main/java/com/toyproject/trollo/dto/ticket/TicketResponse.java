package com.toyproject.trollo.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "티켓 응답 DTO")
public record TicketResponse(
        @Schema(description = "티켓 ID", example = "1001")
        Long id,

        @Schema(description = "티켓 제목", example = "API 스펙 정리")
        String title,

        @Schema(description = "티켓 설명", example = "요청/응답 필드를 확정합니다.")
        String description,

        @Schema(description = "보드 내 티켓 위치", example = "3")
        int position,

        @Schema(description = "보드 ID", example = "101")
        Long boardId
) {
}
