package com.games_price_tracker.api.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.page_dto.PageDto;
import com.games_price_tracker.api.page_dto.PageDtoMapper;

import jakarta.validation.constraints.Min;

import org.hibernate.validator.constraints.Range;
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
    private final PageDtoMapper pageDtoMapper;

    GameController(GameService gameService, GameMapper gameMapper, PageDtoMapper pageDtoMapper){
        this.gameService = gameService;
        this.gameMapper = gameMapper;
        this.pageDtoMapper = pageDtoMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameInfo> getGameInfo(@PathVariable Long id) {
        Game game = gameService.getGameById(id);

        return ResponseEntity.ok(gameMapper.toGameInfo(game));
    }  
    
    @GetMapping()
    public ResponseEntity<PageDto<GameInfo>> getGames(
        @RequestParam(required = false) String name, 
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page debe ser mayor o igual a 0") int page, 
        @RequestParam(defaultValue = "10") @Range(min = 1, max = 50, message = "Size debe estar entre 1 y 50") int size
    ) { 
        Page<Game> games = gameService.getGames(name, PageRequest.of(page, size));
        
        Page<GameInfo> gamesInfo = games.map(game -> gameMapper.toGameInfo(game));

        PageDto<GameInfo> gameInfoPage = pageDtoMapper.fromPage(gamesInfo);

        return ResponseEntity.ok(gameInfoPage);
    }
}
