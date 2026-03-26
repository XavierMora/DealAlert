package com.games_price_tracker.api.account;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccountEmailCooldown {
    private final Duration intervalSendEmail;

    AccountEmailCooldown(@Value("${account.sign-in-code-email-interval}") Duration intervalSendEmail){
        this.intervalSendEmail = intervalSendEmail;
    }
    
    public boolean canSendSignInCode(Instant lastSignInCodeSentAt){
        if(lastSignInCodeSentAt == null) return true;

        long secondsSinceLastEmail = lastSignInCodeSentAt.until(Instant.now(), ChronoUnit.SECONDS);

        return secondsSinceLastEmail > intervalSendEmail.getSeconds();
    }

    public Duration timeUntilNextSignInCodeSend(Instant lastSignInCodeSentAt){
        if(lastSignInCodeSentAt == null) return Duration.ofSeconds(0);

        long secondsSinceLastEmail = lastSignInCodeSentAt.until(Instant.now(), ChronoUnit.SECONDS);

        return intervalSendEmail.minusSeconds(secondsSinceLastEmail);
    }
}
