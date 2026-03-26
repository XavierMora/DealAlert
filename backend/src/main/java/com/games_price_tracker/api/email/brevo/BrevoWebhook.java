package com.games_price_tracker.api.email.brevo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.account.AccountCacheService;
import com.games_price_tracker.api.account.AccountService;
import com.games_price_tracker.api.core.response.ApiResponseBody;
import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.core.response.ErrorCode;

import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/brevo")
public class BrevoWebhook {
    private final AccountService accountService;
    private final String token;
    private final ObjectMapper objectMapper;

    BrevoWebhook(@Value("${brevo.webhook.token}") String token, AccountService accountService, AccountCacheService accountCacheService, ObjectMapper objectMapper){
        this.token = token;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("/sign-in-code-error")
    public ResponseEntity<ApiResponseBody<Void>> signInCodeError(@RequestBody String body, @RequestHeader String authorization) {
        String token = authorization.replace("Bearer ", "");
        
        if(!this.token.equals(token)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseBodyBuilder.error(ErrorCode.FORBIDDEN));
        }
        
        String email = objectMapper.readTree(body).get("email").asString();
        accountService.clearLastSignInCodeSentAt(email);
        
        return ResponseEntity.noContent().build();
    }
    
}
