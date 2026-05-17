package com.games_price_tracker.api.telegram_bot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.games_price_tracker.api.account.AccountService;
import com.games_price_tracker.api.core.response.ApiResponseBody;
import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.core.response.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/telegram/webhook")
public class TelegramWebhook {
    private final String tokenWebhook;
    private final AccountService accountService;

    public TelegramWebhook(@Value("${telegram.webhook.token}") String tokenWebhook, AccountService accountService){
        this.tokenWebhook = tokenWebhook;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseBody<Void>> verifyToken(@RequestBody() Update update, @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token") String secretToken) {
        if(!tokenWebhook.equals(secretToken)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseBodyBuilder.error(ErrorCode.FORBIDDEN));
        }

        String msg = update.getMessage().getText();
        
        if(!msg.startsWith("/start ")) return ResponseEntity.noContent().build();

        String accountToken = msg.replace("/start ", "");
        accountService.linkTelegramAccount(accountToken, update.getMessage().getFrom().getId());

        return ResponseEntity.ok().build();
    }   
}
