package com.games_price_tracker.api.email.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailConfigProperties {
    private final String from;
    private final Duration signInCodeInterval;

    EmailConfigProperties(String from, Duration signInCodeInterval){
        this.from = from;
        this.signInCodeInterval = signInCodeInterval;
    }

    public String getFrom() {
        return from;
    }

    public Duration getSignInCodeInterval() {
        return signInCodeInterval;
    }
}
