package com.games_price_tracker.api.account;

import java.time.Duration;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

@Service
public class AccountCacheService {
    public AccountCacheService(){}
    
    // Cacheo de bucket con 1 token/seg de un email
    @Cacheable(cacheNames = "email-bucket", key = "#email", sync = true)
    public Bucket getBucket(String email){
        return Bucket.builder()
        .addLimit(limit -> limit.capacity(1).refillIntervally(1, Duration.ofSeconds(1)).initialTokens(1))
        .build();
    }
}
