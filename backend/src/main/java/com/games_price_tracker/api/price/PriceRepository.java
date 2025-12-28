package com.games_price_tracker.api.price;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceRepository extends JpaRepository<Price, Long>{
    @Modifying(clearAutomatically = true)
    @Query("update Price p set p.lastUpdate=:timestamp where p.id=:id")
    int updateLastUpdate(@Param("id") Long id, @Param("timestamp") Instant timestamp);
}
