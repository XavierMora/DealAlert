package com.games_price_tracker.api.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.steam.AppSteam;

@SpringBootTest
public class GameMapperTest {
    @Autowired
    GameMapper gameMapper;

    @Test
    void shouldMapFromAppSteamToGame(){
        AppSteam appSteam = new AppSteam(123L, "test");
        Game game = gameMapper.fromAppSteam(appSteam);

        assertEquals(123L, game.getSteamId());
        assertEquals("test", game.getName());
    }
}
