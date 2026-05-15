package com.games_price_tracker.api.account.sign_in_code;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.TokenGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class SignInCodeHandler {
    private final Cache<String, String> codeCache;
    private final TokenGenerator<String> tokenGenerator;

    public SignInCodeHandler(@Value("${account.sign-in-code-duration}") Duration codeDuration, TokenGenerator<String> signInCodeGenerator){
        codeCache = Caffeine.newBuilder().expireAfterWrite(codeDuration).maximumSize(10_000).build();
        tokenGenerator = signInCodeGenerator;
    }

    public String getOrCreate(String email){
        return codeCache.get(email, (k) -> {
            return tokenGenerator.generate();
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
