package com.games_price_tracker.api.price_change_alert;

import java.time.Duration;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.Account;

import io.github.bucket4j.Bucket;

@Service
public class PriceChangeAlertCacheService {
    @Cacheable(cacheNames = "alerts-rate-limit", sync = true, key = "#a0.getEmail()")
    public Bucket getBucket(Account account){
        return Bucket.builder().addLimit(limit -> limit.capacity(3).refillGreedy(10, Duration.ofMinutes(1))).build();
    }
}
