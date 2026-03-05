package com.games_price_tracker.api.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.games_price_tracker.api.account.dtos.AccountDto;
import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.common.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.session_token.SessionToken;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    private AccountCacheService accountCacheService;

    public AccountController(AccountService accountService, AccountCacheService accountCacheService){
        this.accountService = accountService;
        this.accountCacheService = accountCacheService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<ApiResponseBody<Void>> signInCode(
        @RequestBody @Valid SignInBody body
    ) {
        accountCacheService.setEmailSentCache(body.email());
        
        try {
            Instant sentAt = accountService.sendSignInCode(body.email());
            accountCacheService.updateEmailSentCache(body.email(), sentAt);
        } catch (RuntimeException e) {
            accountCacheService.evictEmailSentCache(body.email());
            throw e;
        }

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

    @GetMapping()
    public ResponseEntity<ApiResponseBody<AccountDto>> getAccount(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponseBodyBuilder.success(
            new AccountDto(account.getId(), account.getEmail()))
        );
    }
}
