package com.games_price_tracker.api.common;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
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
    @Value("${app.email.sign-in-code.interval}")
    private Duration intervalSendEmail;

    private Cache<Object, Object> accountRateLimitCache(){
        return Caffeine.newBuilder().maximumSize(5_000).expireAfterWrite(Duration.ofMinutes(5)).build();
    }

    private Cache<Object, Object> emailSentCache(){
        return Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(intervalSendEmail).build();
    }

    @Bean
    CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("account-rate-limit", accountRateLimitCache());
        cacheManager.registerCustomCache("email-sent", emailSentCache());
        return cacheManager;
    }
}
