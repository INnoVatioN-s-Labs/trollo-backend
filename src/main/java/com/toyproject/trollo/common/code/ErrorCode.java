package com.toyproject.trollo.common.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_EMAIL_DUPLICATED("-2001", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAILED("-8408", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    VALIDATION_FAILED("-8404", "요청값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("-8500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
