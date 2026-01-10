package com.games_price_tracker.api.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.page_dto.PageDto;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


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
    
    @GetMapping()
    public ResponseEntity<PageDto<GameInfo>> getGames(@RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 30));
        Page<Game> games = gameService.getGames(name, PageRequest.of(page, size));
        
        PageDto<GameInfo> gameInfoPage = gameMapper.fromGamePagetoGameInfoPage(games);

        return ResponseEntity.ok(gameInfoPage);
    }
}
