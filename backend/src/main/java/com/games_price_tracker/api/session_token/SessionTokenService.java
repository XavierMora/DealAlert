package com.games_price_tracker.api.session_token;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.account.Account;

@Service
public class SessionTokenService {
    private final Duration durationValidToken = Duration.ofDays(7);
    private final SessionTokenRepository sessionTokenRepository;

    public SessionTokenService(SessionTokenRepository sessionTokenRepository){
        this.sessionTokenRepository = sessionTokenRepository;
    }

    public SessionToken createSessionToken(Account account){
        SessionToken sessionToken = new SessionToken(account);
        sessionToken.assignToken(UUID.randomUUID(), durationValidToken);
        
        return sessionToken;
    }

    @Transactional
    public void invalidateToken(UUID token){
        sessionTokenRepository.deleteByToken(token);
    }
}
