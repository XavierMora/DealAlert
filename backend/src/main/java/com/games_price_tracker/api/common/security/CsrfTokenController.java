package com.games_price_tracker.api.common.security;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/csrf")
public class CsrfTokenController {
    // Endpoint para generar csrf token, que se genera en la security chain
    @GetMapping()
    public ResponseEntity<Void> csrfToken() {
        return ResponseEntity.noContent().build();
    }
}
