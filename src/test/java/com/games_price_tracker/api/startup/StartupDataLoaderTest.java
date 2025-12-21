package com.games_price_tracker.api.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameService;

@SpringBootTest
public class StartupDataLoaderTest {
    @Autowired
    GameService gameService;

    @Test
    void shouldBeDataLoaded(){
        List<Game> games = gameService.getAllGames();

        assertEquals(10, games.size());
     
        Game someGame = games.get(0);
        assertNotNull(someGame.getId());
        assertNotNull(someGame.getName());
        assertNotNull(someGame.getSteamId());
    }
}
