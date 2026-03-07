package com.toyproject.trollo.common.exception;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.ReturnMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.validation.ConstraintViolationException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException은 ErrorCode의 HttpStatus와 메시지로 응답한다")
    void handleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.USER_EMAIL_DUPLICATED);

        ReturnMessage<Void> response = globalExceptionHandler.handleBusinessException(exception);

        assertThat(response.getResult()).isEqualTo(ErrorCode.USER_EMAIL_DUPLICATED.getCode());
        assertThat(response.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("BadCredentialsException은 AUTHENTICATION_FAILED로 응답한다")
    void handleBadCredentialsException() {
        ReturnMessage<Void> response = globalExceptionHandler
                .handleBadCredentialsException(new BadCredentialsException("bad credentials"));

        assertThat(response.getResult()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED.getMessage());
    }

    @Test
    @DisplayName("ConstraintViolationException은 VALIDATION_FAILED로 응답한다")
    void handleValidationException() {
        ReturnMessage<Void> response = globalExceptionHandler
                .handleValidationException(new ConstraintViolationException(Collections.emptySet()));

        assertThat(response.getResult()).isEqualTo(ErrorCode.VALIDATION_FAILED.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.VALIDATION_FAILED.getMessage());
    }
}
