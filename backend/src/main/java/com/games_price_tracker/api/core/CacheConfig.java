package com.games_price_tracker.api.core;

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
    private final Duration intervalSendEmail;

    CacheConfig(@Value("${account.sign-in-code-email-interval}") Duration intervalSendEmail){
        this.intervalSendEmail = intervalSendEmail;
    }

    private Cache<Object, Object> verifyCodeRateLimitCache(){
        return Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    private Cache<Object, Object> emailSentCache(){
        return Caffeine.newBuilder().maximumSize(3_000).expireAfterWrite(intervalSendEmail).build();
    }

    private Cache<Object, Object> accountRateLimitCache(){
        return Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    private Cache<Object, Object> publicRateLimitCache(){
        return Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    @Bean
    CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("verify-code-rate-limit", verifyCodeRateLimitCache());
        cacheManager.registerCustomCache("email-sent", emailSentCache());
        cacheManager.registerCustomCache("account-rate-limit", accountRateLimitCache());
        cacheManager.registerCustomCache("public-rate-limit", publicRateLimitCache());
        return cacheManager;
    }
}
