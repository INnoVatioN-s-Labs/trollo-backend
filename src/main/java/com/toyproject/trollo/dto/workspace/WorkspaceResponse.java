package com.toyproject.trollo.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크스페이스 응답 DTO")
public record WorkspaceResponse(
        @Schema(description = "워크스페이스 ID", example = "10")
        Long id,
        @Schema(description = "워크스페이스 이름", example = "백엔드")
        String name,
        @Schema(description = "워크스페이스 설명", example = "백엔드 작업 공간")
        String description,
        @Schema(description = "초대 코드", example = "AB12CD34")
        String inviteCode
) {
}
