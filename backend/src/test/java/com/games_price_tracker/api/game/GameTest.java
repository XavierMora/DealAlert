package com.games_price_tracker.api.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.price.Price;
import com.games_price_tracker.api.steam.AppSteam;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class GameTest {
    private final GameService gameService;
    private final GameMapper gameMapper;
    private Game game;
    
    @Autowired
    GameTest(GameService gameService, GameMapper gameMapper){
        this.gameService=gameService;
        this.gameMapper=gameMapper;
    }

    @BeforeEach
    void setup(){
        game = gameService.createGame(1L, "test");
    }

    @Test
    void gamePriceShouldNotNeedUpdate(){
        game.setPrice(new Price(6,4,game));

        assertEquals(false, gameService.gamePriceNeedsUpdate(game));
    }

    @Value("${price.min-interval-update}") 
    private Duration priceMinIntervalUpdate;

    @Test
    void gamePriceShouldNeedUpdate(){
        assertEquals(true, gameService.gamePriceNeedsUpdate(game)); // sin precio

        Price price = new Price(6,4,game);
        game.setPrice(price);
        price.setLastUpdate(Instant.now().minus(priceMinIntervalUpdate.plusHours(1)));
        assertEquals(true, gameService.gamePriceNeedsUpdate(game));
    }

    @Test
    void shouldMapFromAppSteamToGame(){
        AppSteam appSteam = new AppSteam(123L, "test");
        game = gameMapper.fromAppSteam(appSteam);

        assertEquals(123L, game.getSteamId());
        assertEquals("test", game.getName());
    }

    @Test
    void shouldMapToGameInfo(){
        GameInfo gameInfo = gameMapper.toGameInfo(game);
        assertNull(gameInfo.priceInfo());
        
        Price price = new Price(2, 1, game);
        game.setPrice(price);

        gameInfo = gameMapper.toGameInfo(game);
        assertNotNull(gameInfo.priceInfo());

        assertNotNull(gameInfo.id());
        assertEquals(1L, gameInfo.SteamId());
        assertEquals("test", gameInfo.name());
    }
}
