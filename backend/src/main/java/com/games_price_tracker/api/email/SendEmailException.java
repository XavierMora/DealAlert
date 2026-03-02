package com.games_price_tracker.api.email;

public class SendEmailException extends RuntimeException {
    public SendEmailException(Throwable cause){
        super("Hubo un problema con el envío del email.", cause);
    }
}
