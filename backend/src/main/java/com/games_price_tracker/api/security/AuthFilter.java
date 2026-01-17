package com.games_price_tracker.api.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.games_price_tracker.api.session_token.SessionToken;
import com.games_price_tracker.api.session_token.SessionTokenService;
import com.games_price_tracker.api.session_token.exceptions.InvalidSessionTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final SessionTokenService sessionTokenService;

    public AuthFilter(SessionTokenService sessionTokenService){
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie sessionCookie = getSessionCookie(request.getCookies());

        if(sessionCookie == null) throw new BadCredentialsException("Token is null");
        
        SessionToken sessionToken;
        try {
            UUID token = UUID.fromString(sessionCookie.getValue());
            sessionToken = sessionTokenService.getSessionToken(token);
        } catch (IllegalArgumentException | InvalidSessionTokenException e) {
            throw new BadCredentialsException("Invalid token");
        } 
        
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();

        Authentication authentication = new UsernamePasswordAuthenticationToken(sessionToken.getAccount(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        filterChain.doFilter(request, response);
    }

    private Cookie getSessionCookie(Cookie[] cookies){
        if(cookies == null) return null;

        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("SESSION")) return cookie;
        }
        
        return null;
    }
}
