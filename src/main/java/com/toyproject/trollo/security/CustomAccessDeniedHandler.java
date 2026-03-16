package com.toyproject.trollo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.ReturnMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) 
            throws IOException, ServletException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK 강제

        ReturnMessage<Void> returnMessage = new ReturnMessage<>(ErrorCode.ACCESS_DENIED);
        response.getWriter().write(objectMapper.writeValueAsString(returnMessage));
    }
}
