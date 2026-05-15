package com.games_price_tracker.api.account.sign_in_code;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.games_price_tracker.api.core.TokenGenerator;

@Component
public class SignInCodeGenerator implements TokenGenerator<String> {
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        return String.valueOf(secureRandom.nextInt(100000, 1000000));
    }
}
