package com.games_price_tracker.api.startup;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import com.games_price_tracker.api.game.Game;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@SpringBootTest
@WireMockTest(httpPort = 8081)
public class SteamAppListClientTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    void setupMockApi() throws IOException{
        String data = Files.readString(resourceLoader.getResource("classpath:some-steam-data.json").getFilePath());
        
        stubFor(get(urlEqualTo("/IStoreService/GetAppList/v1"))
        .willReturn(aResponse().withBody(data)));
    }

    @Autowired
    SteamAppListClient steamAppListClient;

    @Test
    void shouldGetGamesFromAppListApi(){
        List<Game> games = steamAppListClient.getGamesFromAppListApi();

        assertNotNull(games);
        assertEquals(10, games.size());
    }
}
