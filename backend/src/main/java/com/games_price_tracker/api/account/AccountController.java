package com.games_price_tracker.api.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.dtos.VerifyCodeBody;
import com.games_price_tracker.api.account.enums.SignInCodeResult;
import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.session_token.SessionToken;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<ApiResponseBody> signInCode(
        @Valid @RequestBody SignInBody body, 
        @RequestHeader(name = "Device-ID") @UUID(message = "Header Device-ID no tiene formato válido.") String deviceId
    ) {
        SignInCodeResult codeResult = accountService.signInCode(body.email(), deviceId);
        BodyBuilder responseWithStatus = ResponseEntity.status(HttpStatus.OK);

        if(codeResult == SignInCodeResult.TOO_MANY_REQUESTS){
            responseWithStatus = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        return responseWithStatus.body(new ApiResponseBody(codeResult.getMessage(), null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(
        @RequestBody @Valid VerifyCodeBody body, 
        @RequestHeader("Device-ID") @UUID(message = "Header Device-ID no tiene formato válido.") String deviceId
    ) {
        SessionToken sessionToken =  accountService.verifyCode(body.email(), body.code(), deviceId);        

        HttpHeaders headers = new HttpHeaders();

        Long maxAge = Instant.now().until(sessionToken.getExpiration(), ChronoUnit.SECONDS);
        headers.set("Set-Cookie", sessionCookie(sessionToken.getToken().toString(), maxAge.intValue()));

        return ResponseEntity.ok().headers(headers).build();
    }    

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "SESSION") String sessionToken) {
        accountService.logout(sessionToken);
        
        HttpHeaders headers = new HttpHeaders();

        headers.set("Set-Cookie", sessionCookie(null, 0));

        return ResponseEntity.noContent().headers(headers).build();
    }
    
    private String sessionCookie(String sessionValue, int maxAge){
        return String.format("SESSION=%s; HttpOnly; SameSite=Lax; Max-Age=%d; Secure; Path=/", sessionValue, maxAge);
    }
}
