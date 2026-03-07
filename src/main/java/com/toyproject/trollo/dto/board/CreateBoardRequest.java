package com.toyproject.trollo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "보드 생성 요청 DTO")
public record CreateBoardRequest(
        @Schema(description = "보드 이름", example = "Todo", maxLength = 50)
        @NotBlank(message = "보드 이름은 필수입니다.")
        @Size(max = 50, message = "보드 이름은 50자 이하여야 합니다.")
        String name
) {
}
