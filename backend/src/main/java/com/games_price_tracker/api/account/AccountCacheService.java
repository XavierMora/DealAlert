package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.SignInCodeEmailCooldownException;
import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;

import io.github.bucket4j.Bucket;

@Service
public class AccountCacheService {
    private final AccountEmailCooldown accountEmailCooldown;
    private final Cache emailSentCache;

    public AccountCacheService(CacheManager cacheManager, AccountEmailCooldown accountEmailCooldown){
        this.emailSentCache = cacheManager.getCache("email-sent");
        this.accountEmailCooldown = accountEmailCooldown;
    }

    @Cacheable(cacheNames = "verify-code-rate-limit", sync = true)
    public Bucket getBucketVerifyCode(String email){
        return Bucket.builder()
        .addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(3)))
        .build();
    }

    // Se setea primero con un optional vacio, si llegan más requests y no terminó la anterior, se lanza la primera excepción
    // Cuando termina la request que la seteo se actualiza con la fecha, entonces si llega otra y no paso el intervalo de envio se lanza la otra excepción
    @SuppressWarnings("unchecked")
    public void setEmailSentCache(String email){
        ValueWrapper value = emailSentCache.putIfAbsent(email, Optional.empty());

        if(value == null) return; 

        Optional<Instant> optionalEmailSentAt = (Optional<Instant>) value.get();
        
        if(optionalEmailSentAt.isEmpty()){
            throw new TooManyRequestsException();
        }else{            
            throw new SignInCodeEmailCooldownException(
                accountEmailCooldown.timeUntilNextSignInCodeSend(optionalEmailSentAt.get()).getSeconds(), 
                TimeUnit.SECONDS
            );
        }
    }

    public void updateEmailSentCache(String email, Instant emailSentAt){
        emailSentCache.put(email, Optional.of(emailSentAt));
    }

    public void evictEmailSentCache(String email){
        emailSentCache.evictIfPresent(email);
    }

    @Cacheable(cacheNames = "account-rate-limit", sync = true)
    public Bucket getBucketAccount(String email){
        return Bucket.builder().addLimit(limit -> limit.capacity(30).refillGreedy(30, Duration.ofSeconds(30))).build();
    }
}
