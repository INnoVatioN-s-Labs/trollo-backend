package com.toyproject.trollo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보드 응답 DTO")
public record BoardResponse(
        @Schema(description = "보드 ID", example = "101")
        Long id,
        @Schema(description = "보드 이름", example = "Todo")
        String name,
        @Schema(description = "보드 위치", example = "1")
        int position,
        @Schema(description = "워크스페이스 ID", example = "10")
        Long workspaceId
) {
}
