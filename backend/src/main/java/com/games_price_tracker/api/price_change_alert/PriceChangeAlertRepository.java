package com.games_price_tracker.api.price_change_alert;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceChangeAlertRepository extends JpaRepository<PriceChangeAlert, Long> {
    Optional<PriceChangeAlert> findByAccountIdAndGameId(Long accountId, Long gameId);

    Page<PriceChangeAlert> findAllByAccountId(Long accountId, Pageable pageable);

    int deleteByAccountIdAndGameId(Long accountId, Long gameId);

    Optional<List<PriceChangeAlert>> findAllByGameId(Long gameId);
}
