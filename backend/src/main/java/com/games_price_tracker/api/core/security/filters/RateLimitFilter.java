package com.games_price_tracker.api.core.security.filters;

import java.io.IOException;
import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.core.response.ErrorCode;
import com.github.benmanes.caffeine.cache.Cache;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class RateLimitFilter extends OncePerRequestFilter{
    private final Cache<Object, Object> cacheRateLimit;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    RateLimitFilter(CacheManager cacheManager, ObjectMapper objectMapper){
        this.cacheRateLimit = (Cache<Object, Object>) cacheManager.getCache("public-rate-limit").getNativeCache();
        this.objectMapper = objectMapper;
    }

    private Bucket createBucket(){
        return Bucket.builder().addLimit(limit -> 
        limit.capacity(100)
        .refillGreedy(50, Duration.ofMinutes(1)))
        .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Bucket bucket = (Bucket) cacheRateLimit.get(request.getRemoteAddr(), (k) -> createBucket());
        
        if(bucket.tryConsume(1)){
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write(
            objectMapper.writeValueAsString(
                ApiResponseBodyBuilder.error(
                    "Muchas peticiones. Intentar más tarde.", 
                    ErrorCode.TOO_MANY_REQUESTS
                )
            )
        );        
    }
}
