package com.games_price_tracker.api.core;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {
    private Cache<Object, Object> publicRateLimitCache(){
        return Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    @Bean
    CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("public-rate-limit", publicRateLimitCache());
        return cacheManager;
    }
}
