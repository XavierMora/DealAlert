package com.games_price_tracker.api.account.exceptions;

public class AccountAuthErrorException extends RuntimeException {
    private final String message;

    public AccountAuthErrorException(String msg){
        super(msg);
        this.message = msg;
    }

    public String getMessage() {
        return message;
    }
}
