package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.games_price_tracker.api.account.exceptions.AuthError;
import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;

public class AccountRateLimitTest {
    private AccountRateLimit accountRateLimit;

    @BeforeEach
    void setup(){
        accountRateLimit = new AccountRateLimit(Duration.ofDays(365));
    }

    @Test
    void shouldThrowWhenRateLimitIsExceeded(){
        String email = "email";

        for (int i = 0; i < 30; i++) {
            accountRateLimit.checkAccountRequestLimit(email);
        }

        assertThrows(TooManyRequestsException.class, () -> accountRateLimit.checkAccountRequestLimit(email));
        assertDoesNotThrow(() -> accountRateLimit.checkAccountRequestLimit("email2"));
    }

    @Test
    void shouldThrowWhenMaxAttemptsLimitIsExceeded(){
        String email = "email";

        for (int i = 0; i < 5; i++) {
            accountRateLimit.checkVerificationCodeAttemptLimit(email);
        }

        TooManyRequestsException ex = assertThrows(TooManyRequestsException.class, () -> accountRateLimit.checkVerificationCodeAttemptLimit(email));
        assertEquals(AuthError.MAX_ATTEMPTS_REACHED, ex.getErrorCode());
        assertDoesNotThrow(() -> accountRateLimit.checkVerificationCodeAttemptLimit("email2"));
    }
}
