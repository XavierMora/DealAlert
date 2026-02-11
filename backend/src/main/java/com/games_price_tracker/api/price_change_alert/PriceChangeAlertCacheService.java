package com.games_price_tracker.api.price_change_alert;

import java.time.Duration;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

@Service
public class PriceChangeAlertCacheService {
    private final PriceChangeAlertRepository priceChangeAlertRepository;

    PriceChangeAlertCacheService(PriceChangeAlertRepository priceChangeAlertRepository){
        this.priceChangeAlertRepository = priceChangeAlertRepository;
    }

    @Cacheable(cacheNames = "alerts-rate-limit", sync = true)
    public Bucket getBucket(String email){
        return Bucket.builder().addLimit(limit -> limit.capacity(10).refillGreedy(10, Duration.ofMinutes(1))).build();
    }

    @Cacheable(cacheNames = "alerts", sync = true, key = "#accountId", condition = "#pageable.getPageNumber() == 0")
    public Page<PriceChangeAlert> getAlerts(Long accountId, Pageable pageable){
        return priceChangeAlertRepository.findAllByAccountId(accountId, pageable);
    }

    @CacheEvict(cacheNames = "alerts", key = "#accountId")
    public void evictAlertsCache(Long accountId){
    }
}
