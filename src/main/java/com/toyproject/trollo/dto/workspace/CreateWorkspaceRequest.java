package com.toyproject.trollo.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "워크스페이스 생성 요청 DTO")
public record CreateWorkspaceRequest(
        @Schema(description = "워크스페이스 이름", example = "백엔드", maxLength = 100)
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(description = "워크스페이스 설명", example = "백엔드 작업 공간", maxLength = 255)
        @Size(max = 255)
        String description
) {
}
