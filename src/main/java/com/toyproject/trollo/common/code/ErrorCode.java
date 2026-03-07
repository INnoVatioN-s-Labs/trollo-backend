package com.toyproject.trollo.common.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_EMAIL_DUPLICATED("-2001", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("-2002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    WORKSPACE_NOT_FOUND("-3001", "워크스페이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    WORKSPACE_ACCESS_DENIED("-3002", "해당 워크스페이스에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    BOARD_NOT_FOUND("-4001", "보드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOARD_INVALID_POSITION("-4002", "유효하지 않은 보드 위치입니다.", HttpStatus.BAD_REQUEST),
    BOARD_WORKSPACE_MISMATCH("-4003", "보드가 해당 워크스페이스에 속하지 않습니다.", HttpStatus.BAD_REQUEST),
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
