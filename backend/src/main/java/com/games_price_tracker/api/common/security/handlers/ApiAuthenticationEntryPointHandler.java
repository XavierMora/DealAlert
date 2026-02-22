package com.games_price_tracker.api.common.security.handlers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.common.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.common.response.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class ApiAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    private ObjectMapper objectMapper;

    public ApiAuthenticationEntryPointHandler(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        response.getWriter().write(
            objectMapper.writeValueAsString(ApiResponseBodyBuilder.error("No autenticado", ErrorCode.UNAUTHORIZED))
        );
    }
}
