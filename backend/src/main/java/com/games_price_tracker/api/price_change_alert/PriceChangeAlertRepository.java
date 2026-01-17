package com.games_price_tracker.api.price_change_alert;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceChangeAlertRepository extends JpaRepository<PriceChangeAlert, Long> {
    Optional<PriceChangeAlert> findByAccountIdAndGameId(Long accountId, Long gameId);
}
