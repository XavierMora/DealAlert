package com.games_price_tracker.api.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.game.dtos.GameInfo;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService gameService;
    private final GameMapper gameMapper;
    
    GameController(GameService gameService, GameMapper gameMapper){
        this.gameService = gameService;
        this.gameMapper = gameMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameInfo> getGameInfo(@PathVariable(required = true) Long id) throws NoSuchElementException {
        Game game = gameService.getGameById(id);

        return ResponseEntity.ok(gameMapper.toGameInfo(game));
    }    
}
