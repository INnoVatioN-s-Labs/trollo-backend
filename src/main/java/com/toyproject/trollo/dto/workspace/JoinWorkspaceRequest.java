package com.toyproject.trollo.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "워크스페이스 참여 요청 DTO")
public record JoinWorkspaceRequest(
        @Schema(description = "워크스페이스 초대 코드", example = "AB12CD34", maxLength = 8)
        @NotBlank
        @Size(min = 8, max = 8)
        @Pattern(regexp = "^[A-Z0-9]{8}$")
        String inviteCode
) {
}
