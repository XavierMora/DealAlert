package com.games_price_tracker.api.game;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private GameRepository gameRepository;

    GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }
    
    public List<Game> getAllGames(){
        return gameRepository.findAll();
    }
}
