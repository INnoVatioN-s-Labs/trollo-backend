package com.toyproject.trollo.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
    public static AuthResponse bearer(String accessToken) {
        return new AuthResponse(accessToken, "Bearer");
    }
}
