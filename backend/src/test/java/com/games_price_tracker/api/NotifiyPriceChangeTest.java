package com.games_price_tracker.api;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.http.ResponseEntity;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.account.AccountRepository;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.game.GameRepository;
import com.games_price_tracker.api.price.PriceService;
import com.games_price_tracker.api.price_change_alert.PriceChangeAlertService;
import com.games_price_tracker.api.steam.AppDetailsSteam;
import com.games_price_tracker.api.tracking.update_game_price.UpdateGamesPricesTask;

import jakarta.transaction.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class NotifiyPriceChangeTest {
    private final SyncTaskExecutor testExecutor = new SyncTaskExecutor();
    private final GameRepository gameRepository;
    private final PriceChangeAlertService priceChangeAlertService;
    private final PriceService priceService;
    private final AccountRepository accountRepository;
    private Game gameTest;
    private List<String> emailsTest = new ArrayList<>(3);
    private AppDetailsSteam appDetailsSteamTest = new AppDetailsSteam();

    @Autowired
    NotifiyPriceChangeTest(GameRepository gameRepository, PriceService priceService, PriceChangeAlertService priceChangeAlertService, AccountRepository accountRepository){
        this.gameRepository = gameRepository;
        this.priceService = priceService;
        this.priceChangeAlertService = priceChangeAlertService;
        this.accountRepository = accountRepository;
    }

    @BeforeEach
    void setup(){
        gameTest = gameRepository.save(new Game(10L, "Counter-Strike"));
        priceService.changePrice(10, 5, gameTest);

        appDetailsSteamTest.setSuccess(true);
        appDetailsSteamTest.setInitialPrice(15);
        appDetailsSteamTest.setFinalPrice(5);
        appDetailsSteamTest.setSteamId(gameTest.getSteamId());

        for (int i = 0; i < 3; i++) {
            Account account = accountRepository.save(
                new Account(String.format("email%d@test",i))
            );

            emailsTest.add(account.getEmail());
            
            priceChangeAlertService.createAlert(account, gameTest.getId());
        }
    }

    @Test
    void notifyAccounts(){
        UpdateGamesPricesTask updateGamesPricesTask = new UpdateGamesPricesTask(
            priceService, 
            List.of(gameTest), 
            List.of(appDetailsSteamTest), 
            priceChangeAlertService
        );

        String urlMailHogApi = "http://localhost:8025/api/v1/messages";
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        testRestTemplate.delete(urlMailHogApi);

        testExecutor.execute(updateGamesPricesTask);

        ObjectMapper objectMapper = new ObjectMapper();
        await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {   
            ResponseEntity<String> response = testRestTemplate.getForEntity(urlMailHogApi, String.class);
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(response.getBody());

            assertEquals(3, arrayNode.size());

            for (int i=0; i < arrayNode.size(); i++) {
                JsonNode items = arrayNode.get(i).get("Content").get("Headers").get("To");
                
                assertTrue(emailsTest.contains(
                    objectMapper.convertValue(items.get(0), String.class)
                ));
            }
        });
    }
}
