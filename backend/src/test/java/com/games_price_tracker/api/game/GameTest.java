package com.games_price_tracker.api.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import com.games_price_tracker.api.price.Price;
import com.games_price_tracker.api.steam.AppSteam;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class GameTest {
    
    @Autowired
    private GameService gameService;


    @Test
    void gameShouldNotNeedPriceUpdate(){
        Game game = new Game(1L, "test");
        game.setPrice(new Price(6,4,game));

        assertEquals(false, gameService.gameNeedsPriceUpdate(game));
    }

    @Test
    void gameShouldNeedPriceUpdate(){
        Game game = new Game(1L, "test");
        Price price = new Price(6,4,game);
        game.setPrice(price);
        price.setLastUpdate(Instant.now().minus(13L, ChronoUnit.HOURS));
        assertEquals(true, gameService.gameNeedsPriceUpdate(game));
    }

    @Test
    void shouldMapFromAppSteamToGame(){
        GameMapper gameMapper = new GameMapper();
        AppSteam appSteam = new AppSteam(123L, "test");
        Game game = gameMapper.fromAppSteam(appSteam);

        assertEquals(123L, game.getSteamId());
        assertEquals("test", game.getName());
    }
}
