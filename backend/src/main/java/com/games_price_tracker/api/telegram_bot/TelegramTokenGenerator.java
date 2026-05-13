package com.games_price_tracker.api.telegram_bot;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.TokenGenerator;

@Component
public class TelegramTokenGenerator implements TokenGenerator<String> {
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] token = new byte[12];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
