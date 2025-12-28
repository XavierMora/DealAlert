package com.games_price_tracker.api.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.game.dtos.GameInfo;
import com.games_price_tracker.api.price.Price;
import com.games_price_tracker.api.steam.AppSteam;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class GameTest {
    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;

    private Game game;

    @BeforeEach
    void setup(){
        game = gameService.createGame(1L, "test");
    }

    @Test
    void gameShouldNotNeedPriceUpdate(){
        game.setPrice(new Price(6,4,game));

        assertEquals(false, gameService.gameNeedsPriceUpdate(game));
    }

    @Test
    void gameShouldNeedPriceUpdate(){
        Price price = new Price(6,4,game);
        game.setPrice(price);
        price.setLastUpdate(Instant.now().minus(13L, ChronoUnit.HOURS));
        assertEquals(true, gameService.gameNeedsPriceUpdate(game));
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
