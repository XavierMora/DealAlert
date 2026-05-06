package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.account.exceptions.SignInEmailCooldownException;
import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class AccountEmailCooldown {
    private final Duration signInEmailInterval;
    private final Cache<String, Optional<Instant>> signInEmailCache;

    AccountEmailCooldown(@Value("${account.sign-in-code-email-interval}") Duration signInEmailInterval){
        this.signInEmailInterval = signInEmailInterval;
        this.signInEmailCache = Caffeine.newBuilder().expireAfterWrite(signInEmailInterval).maximumSize(10_000).build();
    }
    
    private boolean canSendSignInCode(Instant lastSignInCodeSentAt){
        if(lastSignInCodeSentAt == null) return true;

        long secondsSinceLastEmail = lastSignInCodeSentAt.until(Instant.now(), ChronoUnit.SECONDS);

        return secondsSinceLastEmail > signInEmailInterval.getSeconds();
    }

    private Duration timeUntilNextSignInEmailSend(Instant lastSignInCodeSentAt){
        if(lastSignInCodeSentAt == null) return Duration.ofSeconds(0);

        long secondsSinceLastEmail = lastSignInCodeSentAt.until(Instant.now(), ChronoUnit.SECONDS);

        return signInEmailInterval.minusSeconds(secondsSinceLastEmail);
    }

    public Duration getSignInEmailInterval() {
        return signInEmailInterval;
    }

    public void checkSignInEmailCanBeSent(String email, Instant lastSent) throws TooManyRequestsException, SignInEmailCooldownException{
        // Marca con un optional vacío la cache para rechazar otras requests
        Optional<Instant> sentAt = signInEmailCache.asMap().putIfAbsent(email, Optional.empty());
        
        // Si encuentra el optional vacío se lanza la excepción
        if(sentAt != null && sentAt.isEmpty()) throw new TooManyRequestsException();

        if(sentAt == null){
            if(lastSent != null) signInEmailCache.put(email, Optional.of(lastSent));
        }else{
            lastSent = sentAt.get(); // usa el valor cacheado
        }

        if(!canSendSignInCode(lastSent)){
            throw new SignInEmailCooldownException(
                timeUntilNextSignInEmailSend(lastSent).getSeconds(), 
                TimeUnit.SECONDS
            );
        }
    }
    
    public void updateSignInEmailSentAt(String email, Instant emailSentAt){
        signInEmailCache.put(email, Optional.of(emailSentAt));
    }
    
    public void cleanSignInEmailCooldown(String email){
        signInEmailCache.invalidate(email);
    }
}
