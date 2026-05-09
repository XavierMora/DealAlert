package com.games_price_tracker.api.telegram_bot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/telegram/webhook")
public class TelegramWebhook {
    private final Logger log = LoggerFactory.getLogger(TelegramWebhook.class);

    @PostMapping()
    public ResponseEntity<Void> postMethodName(@RequestBody Update entity) {
        log.info(entity.toString());
        return ResponseEntity.noContent().build();
    }   
}
