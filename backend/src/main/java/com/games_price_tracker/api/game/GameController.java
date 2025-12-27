package com.games_price_tracker.api.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService gameService;

    GameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping
    public String getGames(@RequestParam(required = false) String title) {
        
        return new String();
    }
    
}
