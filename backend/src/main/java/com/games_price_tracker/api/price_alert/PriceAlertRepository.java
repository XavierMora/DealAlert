package com.games_price_tracker.api.price_alert;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    Optional<PriceAlert> findByAccountIdAndGameId(Long accountId, Long gameId);
}
