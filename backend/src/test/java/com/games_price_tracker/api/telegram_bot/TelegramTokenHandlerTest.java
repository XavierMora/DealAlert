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
        assertEquals("123", telegramTokenHandler.getToken(1L));
    }

    @Test
    void shouldReturnNullWhenTokenAlreadyExisted(){
        telegramTokenHandler.getToken(1L);
        assertNull(telegramTokenHandler.getToken(2L));
    }

    @Test 
    void shouldReturnAccountIdLinkedToToken(){
        String token = telegramTokenHandler.getToken(1L);
        assertEquals(1L, telegramTokenHandler.getAccountId(token));
    }

    @Test 
    void shouldReturnNullWhenGettingAccountId(){
        assertNull(telegramTokenHandler.getAccountId(""));
    }

    @Test
    void shouldReturnSameTokenForSameAccountId(){
        String t1 = telegramTokenHandler.getToken(1L);
        String t2 = telegramTokenHandler.getToken(1L);
        assertEquals(t1, t2);
    }
}
