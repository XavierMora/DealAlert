package com.games_price_tracker.api.session_token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long>{
    Optional<SessionToken> findByTokenAndDeviceId(String token, String deviceId);
}
