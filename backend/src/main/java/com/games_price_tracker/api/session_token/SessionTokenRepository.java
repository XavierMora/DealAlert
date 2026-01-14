package com.games_price_tracker.api.session_token;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long>{
    void deleteByToken(UUID token);
}
