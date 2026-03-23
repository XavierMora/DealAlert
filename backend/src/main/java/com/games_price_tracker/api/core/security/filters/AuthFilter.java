package com.games_price_tracker.api.core.security.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.games_price_tracker.api.core.security.SessionTokenResolver;
import com.games_price_tracker.api.session_token.SessionToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final SessionTokenResolver sessionTokenResolver;
    private final GrantedAuthority roleUser = new SimpleGrantedAuthority("ROLE_USER");

    public AuthFilter(SessionTokenResolver sessionTokenResolver){
        this.sessionTokenResolver = sessionTokenResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        SessionToken sessionToken = sessionTokenResolver.getToken(request);
        
        if(sessionToken == null){
            filterChain.doFilter(request, response);
            return;
        }
        
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(sessionToken.getAccount(), null, List.of(roleUser));

        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        filterChain.doFilter(request, response);
    }
}
