package com.games_price_tracker.api.game;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.games_price_tracker.api.common.exceptions.ResourceNotFoundException;

@Service
public class GameService {
    private final GameRepository gameRepository;
    @Value("${price.min-interval-update}") 
    private Duration priceMinIntervalUpdate;

    GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }

    public Game getGameById(Long id) throws ResourceNotFoundException{
        return gameRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("El juego no existe."));
    }

    public boolean gamePriceNeedsUpdate(Game game){
        if(game.getPrice() == null) return true;

        Instant lastUpdate = game.getPrice().getLastUpdate();
        Instant limit = lastUpdate.plus(priceMinIntervalUpdate);

        return limit.isBefore(Instant.now());
    }

    @Transactional
    public Game createGame(Long steamId, String name){
        return gameRepository.save(new Game(steamId, name));
    }

    public Page<Game> getGames(String name, Pageable pageable){
        if(name == null || name.isBlank()) return gameRepository.findAll(pageable);
        
        return gameRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    public Long getDateNextUpdateInSeconds(Game game){
        return Instant.now().until(
            game.getPrice().getLastUpdate().plus(priceMinIntervalUpdate), 
            ChronoUnit.SECONDS
        );
    }
}
