package com.games_price_tracker.api.telegram_bot;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.TokenGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class TelegramTokenHandler {
    private final Cache<String, Long> tokenToAccountCache;
    private final Cache<Long, String> accountToTokenCache;
    private final TokenGenerator<String> tokenGenerator;

    TelegramTokenHandler(@Value("${account.telegram-token-duration}") Duration tokenDuration, TokenGenerator<String> telegramTokenGenerator){
        this.tokenToAccountCache = Caffeine.newBuilder().expireAfterWrite(tokenDuration).maximumSize(10_000).build();
        this.accountToTokenCache = Caffeine.newBuilder().expireAfterWrite(tokenDuration).maximumSize(10_000).build();
        this.tokenGenerator = telegramTokenGenerator;
    }

    public String getToken(Long accountId){
        String token = accountToTokenCache.get(accountId, (k) -> {
            String t = tokenGenerator.generate();
            
            if(tokenToAccountCache.asMap().putIfAbsent(t, accountId) == null) return t;
            else return null; // devuelve null si el token generado ya existe
        });

        return token;
    }

    public Long getAccountId(String token){
        return tokenToAccountCache.getIfPresent(token);
    }
}
