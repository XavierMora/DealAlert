package com.games_price_tracker.api.game;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long>{ 
    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
