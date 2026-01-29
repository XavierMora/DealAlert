package com.games_price_tracker.api.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.page_dto.PageDto;
import com.games_price_tracker.api.page_dto.PageDtoMapper;

import jakarta.validation.constraints.Min;

import java.time.Duration;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponseBody> getGameInfo(@PathVariable Long id) {
        Game game = gameService.getGameById(id);

        return ResponseEntity.ok(new ApiResponseBody(
            true,
            null,
            gameMapper.toGameInfo(game)
        ));
    }  
    
    @GetMapping()
    public ResponseEntity<ApiResponseBody> getGames(
        @RequestParam(required = false) String name, 
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page debe ser mayor o igual a 0") int page, 
        @RequestParam(defaultValue = "10") @Range(min = 1, max = 50, message = "Size debe estar entre 1 y 50") int size
    ) { 
        Page<Game> games = gameService.getGames(name, PageRequest.of(page, size, Sort.by("price.lastUpdate").descending()));

        Page<GameInfo> gamesInfo = games.map(game -> gameMapper.toGameInfo(game));

        PageDto<GameInfo> gameInfoPage = pageDtoMapper.fromPage(gamesInfo);

        CacheControl cacheControl;
        if(games.getContent().isEmpty()){
            cacheControl = CacheControl.maxAge(Duration.ofSeconds(60));
        }else{
            Game gameClosestToUpdate = games.getContent().getLast();
            Long maxAge = gameService.getDateNextUpdateInSeconds(gameClosestToUpdate);

            cacheControl = maxAge >= 15 ? CacheControl.maxAge(Duration.ofSeconds(maxAge)) : CacheControl.maxAge(Duration.ofSeconds(0));
        }
        if(name == null || name.isBlank()) cacheControl.cachePublic();
        else cacheControl.cachePrivate();

        return ResponseEntity
        .status(HttpStatus.OK)
        .cacheControl(cacheControl)
        .body(new ApiResponseBody(
            true,
            null,
            gameInfoPage
        ));
    }
}
