package com.games_price_tracker.api.telegram_bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;
import org.thymeleaf.TemplateEngine;

import tools.jackson.databind.ObjectMapper;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.price.dtos.PriceInfo;
import com.games_price_tracker.api.price.dtos.PriceInfoTemplate;
import com.games_price_tracker.api.steam.SteamUrlBuilder;
import com.games_price_tracker.api.telegram_bot.client.TelegramClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TelegramClientTest {
    @Mock private TemplateEngine htmlTemplateEngine;
    @Mock private PriceMapper priceMapper;
    @Mock private SteamUrlBuilder steamUrlBuilder;
    @Mock private TaskScheduler telegramClientScheduler;
    private MockRestServiceServer mockServer;
    private TelegramClient telegramClient;

    @BeforeEach
    void setup(){
        Builder restClient = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClient).build();
        telegramClient = new TelegramClient(restClient.build(), htmlTemplateEngine, priceMapper, steamUrlBuilder, telegramClientScheduler, new ObjectMapper());
    }

    @Test
    void shouldRetryTimesUsingTelegramRetryWhenTooManyRequest(){
        Game game = new Game(1L, "test", "test");
        ChangePriceResult result = new ChangePriceResult(
            new PriceInfo(1L, 10, 10, 0, Instant.now()), 
            new PriceInfo(2L, 10, 10, 0, Instant.now())
        );
        given(priceMapper.fromPriceInfoToPriceInfoTemplate(result.newPrice())).willReturn(new PriceInfoTemplate(0, "", 0, "", 0));
        given(steamUrlBuilder.appUrl(game.getSteamId(), game.getName())).willReturn(URI.create(""));
        given(htmlTemplateEngine.process(eq("deal-notification.html"), any())).willReturn("");
        
        mockServer.expect(requestTo("/sendMessage"))
        .andRespond(withTooManyRequests()
        .body("""
            {
                "parameters": {
                    "retry_after": 10
                }    
            }
            """)
            .contentType(MediaType.APPLICATION_JSON)
        );
        
        telegramClient.sendDealNotification(game, result, List.of(1L));
        
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(telegramClientScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        Instant execTime = Instant.now();
        runnableCaptor.getValue().run();

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(telegramClientScheduler, times(2)).schedule(any(Runnable.class), instantCaptor.capture());

        long secondsUntilNextSend = execTime.until(instantCaptor.getAllValues().getLast(), ChronoUnit.SECONDS);
        assertTrue(secondsUntilNextSend>=10 && secondsUntilNextSend <= 11);

        mockServer.verify();
    }

    @Test
    void shouldRetryTwoTimesWhenTelegramReturnsServerError(){
        Game game = new Game(1L, "test", "test");
        ChangePriceResult result = new ChangePriceResult(
            new PriceInfo(1L, 10, 10, 0, Instant.now()), 
            new PriceInfo(2L, 10, 10, 0, Instant.now())
        );
        given(priceMapper.fromPriceInfoToPriceInfoTemplate(result.newPrice())).willReturn(new PriceInfoTemplate(0, "", 0, "", 0));
        given(steamUrlBuilder.appUrl(game.getSteamId(), game.getName())).willReturn(URI.create(""));
        given(htmlTemplateEngine.process(eq("deal-notification.html"), any())).willReturn("");
        
        mockServer.expect(ExpectedCount.times(3), requestTo("/sendMessage")).andRespond(withServerError());
                
        List<Runnable> scheduledTasks = new ArrayList<>();
        
        given(telegramClientScheduler.schedule(any(Runnable.class), any(Instant.class)))
        .willAnswer(invocation -> {
            scheduledTasks.add(invocation.getArgument(0));
            return null;
        });
        
        telegramClient.sendDealNotification(game, result, List.of(1L));
        
        scheduledTasks.get(0).run();
        scheduledTasks.get(1).run();
        scheduledTasks.get(2).run();

        assertEquals(3, scheduledTasks.size());

        mockServer.verify();
    }
}
