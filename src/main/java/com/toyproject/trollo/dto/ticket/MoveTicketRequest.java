package com.toyproject.trollo.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "티켓 이동 요청 DTO")
public record MoveTicketRequest(
        @Schema(description = "이동할 목표 보드 ID", example = "102")
        @NotNull(message = "이동할 보드 ID는 필수입니다.")
        Long targetBoardId,

        @Schema(description = "이동할 목표 위치(1부터 시작)", example = "2", minimum = "1")
        @NotNull(message = "이동할 위치는 필수입니다.")
        @Min(value = 1, message = "이동할 위치는 1 이상이어야 합니다.")
        Integer targetPosition
) {
}
