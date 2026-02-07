package com.games_price_tracker.api.account.exceptions;

public class AccountAuthErrorException extends RuntimeException {
    private final AuthExceptionError error;

    public AccountAuthErrorException(AuthExceptionError error, String msg){
        super(msg);
        this.error = error;
    }

    public AuthExceptionError getError() {
        return error;
    }
}
