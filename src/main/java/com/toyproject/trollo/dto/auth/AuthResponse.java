package com.toyproject.trollo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답 DTO")
public record AuthResponse(
        @Schema(
                description = "JWT Access Token",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob25nQGV4YW1wbGUuY29tIiwiaWF0IjoxNzEwMDAwMDAwLCJleHAiOjE3MTAwMDM2MDB9.signature"
        )
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType
) {
    public static AuthResponse bearer(String accessToken) {
        return new AuthResponse(accessToken, "Bearer");
    }
}
