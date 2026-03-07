package com.toyproject.trollo.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "티켓 생성 요청 DTO")
public record CreateTicketRequest(
        @Schema(description = "티켓 제목", example = "API 스펙 정리", maxLength = 100)
        @NotBlank(message = "티켓 제목은 필수입니다.")
        @Size(max = 100, message = "티켓 제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "티켓 설명", example = "요청/응답 필드를 확정합니다.")
        String description
) {
}
