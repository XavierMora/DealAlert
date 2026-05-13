package com.games_price_tracker.api.telegram_bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.games_price_tracker.api.core.TokenGenerator;

public class TelegramTokenHandlerTest {
    private TelegramTokenHandler telegramTokenHandler;    
    private TokenGenerator<String> tokenGeneratorTest;

    @BeforeEach
    void setup(){
        tokenGeneratorTest = new TokenGenerator<String>() {
            @Override
            public String generate() {
                return "123";
            }
        };
        this.telegramTokenHandler = new TelegramTokenHandler(Duration.ofMinutes(2), tokenGeneratorTest);
    }

    @Test
    void shouldCreateAndReturnToken(){
        assertEquals("123", telegramTokenHandler.create("email"));
    }

    @Test
    void shouldReturnNullWhenTokenAlreadyExisted(){
        telegramTokenHandler.create("");
        assertNull(telegramTokenHandler.create(""));
    }

    @Test 
    void shouldReturnEmailLinkedToToken(){
        String token = telegramTokenHandler.create("email");
        assertEquals("email", telegramTokenHandler.getEmail(token));
    }

    @Test 
    void shouldReturnNullWhenGettingEmail(){
        assertNull(telegramTokenHandler.getEmail("token"));
    }
}
