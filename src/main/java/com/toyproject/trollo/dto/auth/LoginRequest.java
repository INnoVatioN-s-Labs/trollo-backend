package com.toyproject.trollo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginRequest(
        @Schema(description = "로그인 이메일", example = "hong@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "로그인 비밀번호", example = "password1234")
        @NotBlank
        String password
) {
}
