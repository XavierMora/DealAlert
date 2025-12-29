package com.games_price_tracker.api.game;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }

    public Game getGameById(Long id) throws NoSuchElementException{
        return gameRepository.findById(id).orElseThrow();
    }

    public boolean gameNeedsPriceUpdate(Game game){
        Instant lastUpdate = game.getPrice().getLastUpdate();
        Instant limit = lastUpdate.plus(12L, ChronoUnit.HOURS);

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
}
