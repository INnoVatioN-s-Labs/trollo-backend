package com.toyproject.trollo.common.exception;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.ReturnMessage;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ReturnMessage<Void> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return new ReturnMessage<>(errorCode, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ReturnMessage<Void> handleBadCredentialsException(BadCredentialsException e) {
        ErrorCode errorCode = ErrorCode.AUTHENTICATION_FAILED;
        return new ReturnMessage<>(errorCode);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ReturnMessage<Void> handleValidationException(Exception e) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        return new ReturnMessage<>(errorCode);
    }

    @ExceptionHandler(Exception.class)
    public ReturnMessage<Void> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return new ReturnMessage<>(errorCode);
    }
}
