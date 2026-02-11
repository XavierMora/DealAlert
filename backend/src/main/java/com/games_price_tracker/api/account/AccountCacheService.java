package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;

import io.github.bucket4j.Bucket;

@Service
public class AccountCacheService {
    @Value("${app.email.sign-in-code.interval}")
    private Duration intervalSendEmail;
    private final Cache emailSentCache;

    public AccountCacheService(CacheManager cacheManager){
        this.emailSentCache = cacheManager.getCache("email-sent");
    }

    @Cacheable(cacheNames = "verify-code-rate-limit", sync = true)
    public Bucket getBucket(String email){
        return Bucket.builder()
        .addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(3)))
        .build();
    }

    @SuppressWarnings("unchecked")
    public void setEmailSentCache(String email){
        ValueWrapper value = emailSentCache.putIfAbsent(email, Optional.empty());

        if(value == null) return; 

        Optional<Instant> optionalEmailSentAt = (Optional<Instant>) value.get();
        
        if(optionalEmailSentAt.isEmpty()){
            // Si había un valor y es empty entonces es porque entraron requests muy juntas y se lanza la excepción
            throw new TooManyRequestsException();
        }else{
            // Se calcula hace cuanto fue y se lanza la excepción pasandole el dato
            long emailSentAgo = optionalEmailSentAt.get().until(Instant.now(), ChronoUnit.SECONDS);
            
            throw new TooManyRequestsException(
                intervalSendEmail.minusSeconds(emailSentAgo).getSeconds(), 
                TimeUnit.SECONDS, 
                "Un código fue enviado recientemente. Intentar más tarde.", 
                AuthError.CODE_SENT_RECENTLY
            );
        }
    }

    public void updateEmailSentCache(String email, Instant emailSentAt){
        emailSentCache.put(email, Optional.of(emailSentAt));
    }

    public void evictEmailSentCache(String email){
        emailSentCache.evictIfPresent(email);
    }
}
