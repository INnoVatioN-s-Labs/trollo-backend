package com.toyproject.trollo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "보드 순서 변경 요청 DTO")
public record ReorderBoardRequest(
        @Schema(description = "이동할 목표 위치(1부터 시작)", example = "1", minimum = "1")
        @NotNull(message = "이동할 위치는 필수입니다.")
        @Min(value = 1, message = "이동할 위치는 1 이상이어야 합니다.")
        Integer targetPosition
) {
}
