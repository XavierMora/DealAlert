package com.games_price_tracker.api.game;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }
    
    public List<Game> getAllGames(){
        return gameRepository.findAll();
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
}
