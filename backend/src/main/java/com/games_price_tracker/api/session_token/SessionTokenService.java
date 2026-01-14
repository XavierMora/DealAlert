package com.games_price_tracker.api.session_token;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.games_price_tracker.api.account.Account;

@Service
public class SessionTokenService {
    private final Duration durationValidToken = Duration.ofDays(7);
    
    public SessionToken createSessionToken(String deviceId, Account account){
        SessionToken sessionToken = new SessionToken(deviceId, account);
        sessionToken.assignToken(UUID.randomUUID(), durationValidToken);
        
        return sessionToken;
    }
}
