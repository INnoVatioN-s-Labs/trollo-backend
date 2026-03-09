package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.auth.AuthResponse;
import com.toyproject.trollo.dto.auth.LoginRequest;
import com.toyproject.trollo.dto.auth.SignupRequest;
import com.toyproject.trollo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ReturnMessage<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return new ReturnMessage<>(authService.signup(request));
    }

    @PostMapping("/login")
    public ReturnMessage<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ReturnMessage<>(authService.login(request));
    }
}
