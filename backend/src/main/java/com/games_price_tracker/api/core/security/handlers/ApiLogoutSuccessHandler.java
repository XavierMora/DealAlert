package com.games_price_tracker.api.core.security.handlers;

import java.io.IOException;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.core.response.ErrorCode;
import com.games_price_tracker.api.core.security.SessionTokenResolver;
import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class ApiLogoutSuccessHandler implements LogoutSuccessHandler {
    private final SessionTokenResolver sessionTokenResolver;
    private final SessionTokenService sessionTokenService;
    private final ObjectMapper objectMapper;

    ApiLogoutSuccessHandler(SessionTokenResolver sessionTokenResolver, SessionTokenService sessionTokenService, ObjectMapper objectMapper){
        this.sessionTokenResolver = sessionTokenResolver;
        this.sessionTokenService = sessionTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.NO_CONTENT.value());

        SessionToken sessionToken = sessionTokenResolver.getToken(request);

        if(sessionToken == null) return;
        
        try {
            sessionTokenService.invalidateToken(sessionToken.getToken());

            response.addHeader("Set-Cookie", "SESSION=;Max-Age=0;HttpOnly;Secure;SameSite=None;Path=/");
        } catch (Exception e) { // Por si hay error invalidando el token, no se elimina la cookie session
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponseBodyBuilder.error("Error cerrando sesión", ErrorCode.INTERNAL_SERVER_ERROR))
            );
        }
    }
    
}
