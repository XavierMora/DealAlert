package com.games_price_tracker.api.email;

public class SendEmailException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Hubo un problema con el envío del email.";
    }
}
