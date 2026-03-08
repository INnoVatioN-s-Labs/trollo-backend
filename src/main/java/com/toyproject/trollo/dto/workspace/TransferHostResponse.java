package com.toyproject.trollo.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "호스트 양도 응답 DTO")
public record TransferHostResponse(
        @Schema(description = "워크스페이스 ID", example = "10")
        Long workspaceId,
        @Schema(description = "기존 호스트 사용자 ID", example = "1")
        Long previousHostUserId,
        @Schema(description = "새 호스트 사용자 ID", example = "2")
        Long newHostUserId
) {
}
