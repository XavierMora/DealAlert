package com.games_price_tracker.api.session_token.exceptions;

public enum InvalidStatus {
    NOT_FOUND("Not found"),
    EXPIRED("Expired");

    private String message;

    private InvalidStatus(String message){
        this.message = message; 
    }

    public String getMessage() {
        return message;
    }
}
