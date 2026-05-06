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
import com.games_price_tracker.api.core.response.ApiResponseBody;
import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.session_token.SessionToken;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<ApiResponseBody<Void>> signInCode(
        @RequestBody @Valid SignInBody body
    ) {
        accountService.sendSignInCode(body.email());

        return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponseBodyBuilder.success("Se envió un código al email para iniciar sesión."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(
        @RequestBody @Valid VerifyCodeBody body
    ) {
        SessionToken sessionToken =  accountService.verifyCode(body.email(), body.code());        

        HttpHeaders headers = new HttpHeaders();

        Long maxAge = Instant.now().until(sessionToken.getExpiration(), ChronoUnit.SECONDS);
        headers.set("Set-Cookie", ("SESSION=%s; HttpOnly; SameSite=None; Max-Age=%d; Secure; Path=/").formatted(sessionToken.getToken().toString(), maxAge.intValue()));

        return ResponseEntity.noContent().headers(headers).build();
    }

    @GetMapping()
    public ResponseEntity<ApiResponseBody<AccountDto>> getAccount(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponseBodyBuilder.success(
            new AccountDto(account.getId(), account.getEmail()))
        );
    }
}
