package com.games_price_tracker.api.common.response;

public enum ErrorType implements ApiError {
    INVALID_DATA,
    SENDING_EMAIL,
    TOO_MANY_REQUESTS,
    RESOURCE_NOT_FOUND,
    RESOURCE_ALREADY_EXISTS;

    @Override
    public String getError() {
        return toString();
    }
}
