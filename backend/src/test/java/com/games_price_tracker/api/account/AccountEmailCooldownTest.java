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
    void shouldThrowSignInCodeEmailCooldownExceptionWhenLastSentArgumentHasCooldown(){
        assertThrows(SignInEmailCooldownException.class, () -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest, Instant.now().minusSeconds(2));
        });
    }

    @Test
    void shouldThrowTooManyRequestException(){
        accountEmailCooldown.checkSignInEmailCanBeSent(emailTest, null);

        assertThrows(TooManyRequestsException.class, () -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest, null);
        });
    }

    @Test
    void shouldThrowSignInCodeEmailCooldownExceptionWhenCooldownHasNotPassed(){
        accountEmailCooldown.updateSignInEmailSentAt(emailTest, Instant.now());

        assertThrows(SignInEmailCooldownException.class, () -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest, null);
        });
    }

    @Test
    void shouldAllowSendSignInEmailWhenCooldownHasPassed(){
        Instant a = Instant.now().minus(accountEmailCooldown.getSignInEmailInterval()).minusSeconds(1);
        accountEmailCooldown.updateSignInEmailSentAt(emailTest, a);

        assertDoesNotThrow(() -> {
            accountEmailCooldown.checkSignInEmailCanBeSent(emailTest, null);
        });
    }
}
