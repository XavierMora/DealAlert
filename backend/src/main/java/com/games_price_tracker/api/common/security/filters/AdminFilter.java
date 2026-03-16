package com.games_price_tracker.api.common.security.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminFilter extends OncePerRequestFilter {
    private final GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
    @Value("${app.admin.key}")
    private String adminKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader("Admin-Key");

        if(key == null || !key.equals(adminKey)){
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = contextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null, List.of(adminAuthority));
        context.setAuthentication(authentication);
        contextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }
}
