package com.toyproject.trollo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(
        @Schema(description = "로그인에 사용할 이메일", example = "hong@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호(8자 이상)", example = "password1234", minLength = 8, maxLength = 255)
        @NotBlank
        @Size(min = 8, max = 255)
        String password,

        @Schema(description = "사용자 닉네임", example = "홍길동", maxLength = 50)
        @NotBlank
        @Size(max = 50)
        String nickname
) {
}
