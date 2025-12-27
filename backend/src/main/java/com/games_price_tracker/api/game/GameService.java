package com.games_price_tracker.api.game;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.games_price_tracker.api.price.Price;
import com.games_price_tracker.api.price.PriceService;
import com.games_price_tracker.api.steam.AppDetailsSteam;

import jakarta.persistence.EntityManager;

@Service
public class GameService {
    private final GameRepository gameRepository;
    GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }
    
    public List<Game> getAllGames(){
        return gameRepository.findAll();
    }

    public boolean gameNeedsPriceUpdate(Game game){
        Instant lastUpdate = game.getPrice().getLastUpdate();
        Instant limit = lastUpdate.plus(12L, ChronoUnit.HOURS);

        return limit.isBefore(Instant.now());
    }
}
