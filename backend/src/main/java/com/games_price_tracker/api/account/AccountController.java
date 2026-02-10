package com.games_price_tracker.api.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.common.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.common.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.session_token.SessionToken;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    @Value("${app.email.sign-in-code.interval}")
    private Duration intervalSendEmail;
    private AccountCacheService accountCacheService;

    public AccountController(AccountService accountService, AccountCacheService accountCacheService){
        this.accountService = accountService;
        this.accountCacheService = accountCacheService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<ApiResponseBody<Void>> signInCode(
        @RequestBody @Valid SignInBody body
    ) {
        long emailSentAgo = accountCacheService.emailSentAgo(body.email());
        
        if(emailSentAgo > 0 && emailSentAgo <= intervalSendEmail.get(ChronoUnit.SECONDS)){ // No pasó el intervalo para enviar otro email
            throw new TooManyRequestsException(intervalSendEmail.minusSeconds(emailSentAgo).getSeconds(), TimeUnit.SECONDS, "Un código fue enviado recientemente. Intentar más tarde.");
        }

        accountService.signInCode(body.email());

        return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponseBodyBuilder.success("Se envió un código al email para iniciar sesión."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(
        @RequestBody @Valid VerifyCodeBody body
    ) {
        accountService.verifyCodeRateLimit(body.email());

        SessionToken sessionToken =  accountService.verifyCode(body.email(), body.code());        

        HttpHeaders headers = new HttpHeaders();

        Long maxAge = Instant.now().until(sessionToken.getExpiration(), ChronoUnit.SECONDS);
        headers.set("Set-Cookie", ("SESSION=%s; HttpOnly; SameSite=Lax; Max-Age=%d; Secure; Path=/").formatted(sessionToken.getToken().toString(), maxAge.intValue()));

        return ResponseEntity.ok().headers(headers).build();
    }
}
