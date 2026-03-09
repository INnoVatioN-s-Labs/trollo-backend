package com.toyproject.trollo.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "호스트 양도 요청 DTO")
public record TransferHostRequest(
        @Schema(description = "새 호스트 대상 사용자 ID", example = "2")
        @NotNull
        Long targetUserId
) {
}
