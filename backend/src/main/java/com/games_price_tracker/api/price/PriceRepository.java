package com.games_price_tracker.api.price;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceRepository extends JpaRepository<Price, Long>{
    @Modifying(clearAutomatically = true)
    @Query("update Price p set p.lastUpdate=:lastUpdate where p.id=:id")
    int refreshLastUpdate(@Param("id") Long id, @Param("lastUpdate") Instant lastUpdate);

    Optional<Price> findByGameId(Long gameId);

    @Modifying(clearAutomatically = true)
    @Query("""
        update Price p 
        set p.initialPrice=:initialPrice, p.finalPrice=:finalPrice, p.lastUpdate=:lastUpdate
        where p.id=:id
    """)
    void updatePrice(
        @Param("id") Long id,
        @Param("initialPrice") int initialPrice, 
        @Param("finalPrice") int finalPrice,
        @Param("lastUpdate") Instant lastUpdate
    );
}
