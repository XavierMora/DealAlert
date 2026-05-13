package com.games_price_tracker.api.telegram_bot;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.TokenGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class TelegramTokenHandler {
    private final Cache<String, String> store;
    private final TokenGenerator<String> tokenGenerator;

    TelegramTokenHandler(@Value("${account.telegram-token-duration}") Duration tokenDuration, TokenGenerator<String> telegramTokenGenerator){
        this.store = Caffeine.newBuilder().expireAfterWrite(tokenDuration).maximumSize(10_000).build();
        this.tokenGenerator = telegramTokenGenerator;
    }

    public String create(String email){
        String token = tokenGenerator.generate();
        
        return store.asMap().putIfAbsent(token, email) == null ? token : null;
    }

    public String getEmail(String token){
        return store.getIfPresent(token);
    }
}
