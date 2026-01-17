package com.games_price_tracker.api.price_alert;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.account.Account;
import com.games_price_tracker.api.price_alert.dtos.CreatePriceAlertBody;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/price-alerts")
public class PriceAlertController {
    private final PriceAlertService priceAlertService;

    PriceAlertController(PriceAlertService priceAlertService){
        this.priceAlertService = priceAlertService;
    }

    @PostMapping()
    public ResponseEntity<Map<String, String>> createPriceAlert(@AuthenticationPrincipal Account account, @RequestBody @Valid CreatePriceAlertBody body) {
        boolean success = priceAlertService.createPriceAlert(account, body.gameId());
        
        if(success) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", "Alerta creada."
        ));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Ya existe una alerta para este juego."
        ));
    }
}
