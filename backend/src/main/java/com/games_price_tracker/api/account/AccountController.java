package com.games_price_tracker.api.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.games_price_tracker.api.account.dtos.SignInBody;
import com.games_price_tracker.api.account.enums.SignInCodeResult;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/sign-in-code")
    public ResponseEntity<Map<String, String>> signInCode(@Valid @RequestBody SignInBody account, @RequestHeader(name = "Device-ID") String deviceId) {
        SignInCodeResult codeResult = accountService.signInCode(account, deviceId);

        if(codeResult == SignInCodeResult.TOO_MANY_REQUESTS){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "message", codeResult.getMsg()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", codeResult.getMsg()));
    }
}
