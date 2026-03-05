package com.games_price_tracker.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailConfigProperties {
    private String from;

    EmailConfigProperties(String from){
        this.from = from;
    }

    public String getFrom() {
        return from;
    }
}
