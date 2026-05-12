package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.games_price_tracker.api.account.exceptions.SignInEmailCooldownException;
import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;

public class AccountEmailCooldownTest {
    private AccountEmailCooldown accountEmailCooldown;
    private final String emailTest = "test";

    @BeforeEach
    void setup(){
        accountEmailCooldown = new AccountEmailCooldown(Duration.ofMinutes(2));
    }

    @Test
    void shouldThrowTooManyRequestException(){
        accountEmailCooldown.checkSignInEmailCanBeSent(emailTest);

        assertThrows(TooManyRequestsException.class, () -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest);
        });
    }

    @Test
    void shouldThrowSignInCodeEmailCooldownExceptionWhenCooldownHasNotPassed(){
        accountEmailCooldown.updateSignInEmailSentAt(emailTest, Instant.now());

        assertThrows(SignInEmailCooldownException.class, () -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest);
        });
    }

    @Test
    void shouldAllowSendSignInEmailWhenCooldownHasPassed(){
        Instant time = Instant.now().minus(accountEmailCooldown.getSignInEmailCooldown()).minusSeconds(1);
        accountEmailCooldown.updateSignInEmailSentAt(emailTest, time);

        assertDoesNotThrow(() -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest);
        });
    }
}
