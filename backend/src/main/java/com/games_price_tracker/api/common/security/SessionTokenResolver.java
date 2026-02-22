package com.games_price_tracker.api.common.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;
import com.games_price_tracker.api.session_token.exceptions.InvalidSessionTokenException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SessionTokenResolver {
    private final SessionTokenService sessionTokenService;

    SessionTokenResolver(SessionTokenService sessionTokenService){
        this.sessionTokenService = sessionTokenService;
    }

    public SessionToken getToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        if(cookies == null) return null;

        Cookie sessionCookie = null;

        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("SESSION")){
                sessionCookie = cookie;
                break;
            }
        }

        if(sessionCookie == null) return null;

        SessionToken sessionToken=null;
        // Si hay token se verifica si es válido
        try {
            UUID token = UUID.fromString(sessionCookie.getValue());
            sessionToken = sessionTokenService.getSessionToken(token);
        } catch (IllegalArgumentException | InvalidSessionTokenException e) {
            return null;
        } 

        return sessionToken;
    }
}
