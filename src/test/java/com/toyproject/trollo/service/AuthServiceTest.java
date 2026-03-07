package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.auth.AuthResponse;
import com.toyproject.trollo.dto.auth.LoginRequest;
import com.toyproject.trollo.dto.auth.SignupRequest;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.repository.UserRepository;
import com.toyproject.trollo.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 시 비밀번호를 인코딩하여 저장하고 JWT를 반환한다")
    void signupSuccess() {
        SignupRequest request = new SignupRequest("hong@example.com", "password1234", "홍길동");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-password");
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtTokenProvider.createToken(authentication)).willReturn("jwt-token");

        AuthResponse response = authService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(request.email());
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getNickname()).isEqualTo(request.nickname());

        verify(authenticationManager).authenticate(argThat(auth ->
                request.email().equals(auth.getPrincipal()) && request.password().equals(auth.getCredentials())
        ));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("회원가입 시 이미 존재하는 이메일이면 예외가 발생한다")
    void signupFailsWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest("hong@example.com", "password1234", "홍길동");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request)).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATED);

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    @DisplayName("로그인 성공 시 JWT를 반환한다")
    void loginSuccess() {
        LoginRequest request = new LoginRequest("hong@example.com", "password1234");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtTokenProvider.createToken(authentication)).willReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(argThat(auth ->
                request.email().equals(auth.getPrincipal()) && request.password().equals(auth.getCredentials())
        ));
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
