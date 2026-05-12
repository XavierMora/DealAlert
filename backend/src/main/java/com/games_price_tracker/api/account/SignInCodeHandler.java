package com.games_price_tracker.api.account;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class SignInCodeHandler {
    private final Cache<String, String> codeCache;
    private final SecureRandom secureRandom;

    SignInCodeHandler(@Value("${account.sign-in-code-duration}") Duration codeDuration){
        codeCache = Caffeine.newBuilder().expireAfterWrite(codeDuration).maximumSize(10_000).build();
        secureRandom = new SecureRandom();
    }

    public String getOrCreate(String email){
        return codeCache.get(email, (k) -> {
            return String.valueOf(secureRandom.nextInt(100000, 1000000));
        });
    }

    public boolean codeIsValid(String email, String code){
        String codeStored = codeCache.getIfPresent(email);

        if(codeStored == null || !codeStored.equals(code)) return false;

        return true;
    }

    public void deleteCode(String email){
        codeCache.invalidate(email);
    }
}
