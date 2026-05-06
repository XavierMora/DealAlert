package com.games_price_tracker.api.account;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@Service
public class AccountRateLimit {
    private final Cache<String,Bucket> accountRateLimitCache;
    private final Cache<String,Bucket> verifyCodeCache;

    public AccountRateLimit(){
        this.accountRateLimitCache = Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.verifyCodeCache = Caffeine.newBuilder().maximumSize(5_000).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    public void checkVerificationCodeAttemptLimit(String email) throws TooManyRequestsException{
        Bucket bucket = verifyCodeCache.get(email, k->createBucketVerifyCode());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(!probe.isConsumed()) throw new TooManyRequestsException(
            probe.getNanosToWaitForRefill(), 
            TimeUnit.NANOSECONDS, 
            "Muchos intentos. Intentar más tarde.",
            AuthError.MAX_ATTEMPTS_REACHED
        ); 
    }

    private Bucket createBucketVerifyCode(){
        return Bucket.builder()
        .addLimit(limit -> limit
            .capacity(5)
            .refillIntervally(5, Duration.ofMinutes(3))
        )
        .build();
    }
    
    public void checkAccountRequestLimit(String email) throws TooManyRequestsException{
        Bucket bucket = accountRateLimitCache.get(email, k->createBucketAccount());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(!probe.isConsumed()) throw new TooManyRequestsException(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
    }
    
    private Bucket createBucketAccount(){
        return Bucket.builder()
        .addLimit(limit -> limit
            .capacity(30)
            .refillGreedy(30, Duration.ofSeconds(30))
        )
        .build();
    }
}
