package com.games_price_tracker.api.telegram_bot.client;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import tools.jackson.databind.ObjectMapper;
import com.games_price_tracker.api.game.Game;
import com.games_price_tracker.api.price.PriceMapper;
import com.games_price_tracker.api.price.dtos.ChangePriceResult;
import com.games_price_tracker.api.steam.SteamUrlBuilder;

@Service
public class TelegramClient {
    private final RestClient client;
    private final TemplateEngine htmlTemplateEngine;
    private final PriceMapper priceMapper;
    private final SteamUrlBuilder steamUrlBuilder;
    private final TaskScheduler scheduler;
    private final Logger log = LoggerFactory.getLogger(TelegramClient.class);
    private final ObjectMapper objectMapper;

    public TelegramClient(RestClient telegramBotRestClient, TemplateEngine htmlTemplateEngine, PriceMapper priceMapper, SteamUrlBuilder steamUrlBuilder, TaskScheduler telegramClientScheduler, ObjectMapper objectMapper){
        this.steamUrlBuilder = steamUrlBuilder;
        this.priceMapper = priceMapper;
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.scheduler = telegramClientScheduler;
        this.client = telegramBotRestClient;
        this.objectMapper = objectMapper;
    }

    private void sendHtmlMessageWithRetry(String text, Long telegramUser, int attempt){ 
        client.post().uri((uriBuilder) -> {
            return uriBuilder.path("/sendMessage").build();
        }).body(
            SendMessage.builder()
            .chatId(String.valueOf(telegramUser))
            .text(text)
            .parseMode("HTML")
            .build()
        )
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
            if(response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)){
                try{
                    int retryAfter = objectMapper.readTree(response.getBody()).get("parameters").get("retry_after").asInt();

                    scheduler.schedule(() -> sendHtmlMessageWithRetry(text, telegramUser, attempt+1), Instant.now().plusSeconds(retryAfter));
                }catch(Exception e){
                    log.info("Failed to handle 429 error", e);
                }
            }
        })
        .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
            if(attempt == 3){
                log.warn("Telegram message couldn't be sent to telegram_user_id={} after 3 attempts", telegramUser);
            }else{
                scheduler.schedule(() -> sendHtmlMessageWithRetry(text, telegramUser, attempt+1), Instant.now().plusSeconds(60*attempt));
            }
        })
        .toBodilessEntity();
    }

    public void sendDealNotification(Game game, ChangePriceResult result, List<Long> recipients){
        Context ctx = new Context();
        ctx.setVariable("gameName", game.getName());
        ctx.setVariable(
            "newPrice", 
            priceMapper.fromPriceInfoToPriceInfoTemplate(result.newPrice())
        );
        ctx.setVariable("gameSteamUrl", steamUrlBuilder.appUrl(
            game.getSteamId(), 
            game.getName()
        ).toString());
        
        String template = htmlTemplateEngine.process("deal-notification.html", ctx);

        AtomicInteger count = new AtomicInteger(0);
        recipients.stream().forEach((recipient) -> {
            if(recipient == null) return;

            scheduler.schedule(() -> sendHtmlMessageWithRetry(template, recipient, 1), Instant.now().plusMillis(50*count.get()));
            count.incrementAndGet();
        });
    }
}
