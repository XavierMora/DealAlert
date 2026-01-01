package com.games_price_tracker.api.steam;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.tracker.UpdatePriceTask;
import com.games_price_tracker.api.tracker.UpdatePriceTasksHandler;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
public class SteamClientTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    void setupMockApi() throws IOException{
        String dataAppList = new String(resourceLoader.getResource("classpath:some-steam-data.json").getInputStream().readAllBytes());
        
        stubFor(get(urlEqualTo("/IStoreService/GetAppList/v1"))
        .willReturn(aResponse().withBody(dataAppList)));

        String dataAppDetails = new String(resourceLoader.getResource("classpath:test-appdetails-response.json").getInputStream().readAllBytes());

        stubFor(
            get(urlPathMatching("/api/appdetails?"))
            .withQueryParam("cc", equalTo("AR"))
            .withQueryParam("appids", equalTo("0,1"))
            .withQueryParam("filters", equalTo("price_overview"))
        .willReturn(aResponse().withBody(dataAppDetails)));
    }

    @Autowired
    SteamClient steamClient;

    @Test
    void shouldGetAndParseAppList(){
        List<AppSteam> games = steamClient.getAppList();

        assertNotNull(games);
        assertEquals(10, games.size());
    }

    @Test
    void shouldGetAndParseMultipleAppDetails(){
        List<Long> steamIds = List.of(0L,1L);
        List<AppDetailsSteam> appsDetails = steamClient.getMultipleAppDetails(steamIds);            
        System.out.println(appsDetails.toString());
        assertNotNull(appsDetails);
        assertEquals(2, appsDetails.size());
    }
}
