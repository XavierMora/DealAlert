package com.games_price_tracker.api.game;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.page_dto.PageDto;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.steam.AppSteam;

@Component
public class GameMapper {
    private final PriceMapper priceMapper;

    GameMapper(PriceMapper priceMapper){
        this.priceMapper = priceMapper;
    }
    
    public Game fromAppSteam(AppSteam appSteam){
        return new Game(
            appSteam.getSteamId(), 
            appSteam.getName()
        );
    }

    public GameInfo toGameInfo(Game game){
        PriceInfo priceInfo = null;
        
        if(game.getPrice() != null) priceInfo = priceMapper.toPriceInfo(game.getPrice());
        
        return new GameInfo(
            game.getId(), 
            game.getSteamId(),
            game.getName(),
            priceInfo,
            builderUrlImg(game.getSteamId())
        );
    }

    private String builderUrlImg(Long steamId){
        return String.format("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/%d/header.jpg", steamId);
    }

    public PageDto<GameInfo> fromGamePagetoGameInfoPage(Page<Game> gamePage){
        List<GameInfo> gameInfoContent = gamePage.getContent().stream().map((game) -> toGameInfo(game)).toList();  
        
        return new PageDto<GameInfo>(
            gameInfoContent, 
            gamePage.isEmpty(), 
            gamePage.isFirst(), 
            gamePage.isLast(),
            gamePage.getNumberOfElements(),
            gamePage.getTotalPages(),
            gamePage.getNumber(),
            gamePage.getSize()
        );
    }
}
