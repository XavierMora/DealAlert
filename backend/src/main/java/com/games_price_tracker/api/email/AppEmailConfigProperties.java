package com.games_price_tracker.api.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class AppEmailConfigProperties {
    private String from;
    private String clientVerificationUrl;

    AppEmailConfigProperties(String from, String clientVerificationUrl){
        this.from = from;
        this.clientVerificationUrl = clientVerificationUrl;
    }

    public String getClientVerificationUrl() {
        return clientVerificationUrl;
    }

    public String getFrom() {
        return from;
    }
}
