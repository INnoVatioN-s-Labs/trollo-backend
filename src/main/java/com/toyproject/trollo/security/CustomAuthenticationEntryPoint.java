package com.toyproject.trollo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.ReturnMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) 
            throws IOException, ServletException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK 강제

        ReturnMessage<Void> returnMessage = new ReturnMessage<>(ErrorCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(returnMessage));
    }
}
