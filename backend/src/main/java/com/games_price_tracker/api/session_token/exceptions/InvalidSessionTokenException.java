package com.games_price_tracker.api.session_token.exceptions;

public class InvalidSessionTokenException extends RuntimeException {
    private InvalidStatus invalidStatus;

    public InvalidSessionTokenException(InvalidStatus invalidStatus){
        this.invalidStatus = invalidStatus;
    }

    public InvalidStatus getInvalidStatus() {
        return invalidStatus;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid token: %s", invalidStatus.getMessage());
    }
}
