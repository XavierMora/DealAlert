package com.games_price_tracker.api.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.account.enums.SignInCodeResult;
import com.games_price_tracker.api.session_token.SessionToken;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<Map<String, String>> signInCode(@Valid @RequestBody SignInBody account, @RequestHeader("Device-ID") String deviceId) {
        SignInCodeResult codeResult = accountService.signInCode(account, deviceId);

        if(codeResult == SignInCodeResult.TOO_MANY_REQUESTS){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "message", codeResult.getMsg()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", codeResult.getMsg()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(@RequestBody @Valid VerifyCodeBody verifyCodeBody, @RequestHeader("Device-ID") String deviceId) {
        SessionToken sessionToken =  accountService.verifyCode(verifyCodeBody, deviceId);        

        HttpHeaders headers = new HttpHeaders();

        Long maxAge = Instant.now().until(sessionToken.getExpiration(), ChronoUnit.SECONDS);
        headers.set("Set-Cookie", String.format("session=%s; HttpOnly; SameSite=Lax; Max-Age=%d; Secure; Path=/", sessionToken.getToken().toString(), maxAge.intValue()));

        return ResponseEntity.ok().headers(headers).build();
    }    
}
