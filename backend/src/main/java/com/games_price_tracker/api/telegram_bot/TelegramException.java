package com.games_price_tracker.api.telegram_bot;

public class TelegramException extends RuntimeException{
    private final TelegramError error;

    public TelegramException(TelegramError error, String msg){
        super(msg);
        this.error = error;
    }

    public TelegramError getErrorCode() {
        return error;
    }
}
