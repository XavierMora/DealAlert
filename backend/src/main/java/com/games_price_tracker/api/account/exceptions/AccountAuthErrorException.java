package com.games_price_tracker.api.account.exceptions;

public class AccountAuthErrorException extends RuntimeException {
    private final AuthError error;

    public AccountAuthErrorException(AuthError error, String msg){
        super(msg);
        this.error = error;
    }

    public AuthError getErrorCode() {
        return error;
    }
}
