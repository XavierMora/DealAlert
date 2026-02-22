package com.games_price_tracker.api.common.response;

public enum ErrorCode implements ApiError {
    INVALID_DATA,
    SENDING_EMAIL,
    TOO_MANY_REQUESTS,
    RESOURCE_NOT_FOUND,
    RESOURCE_ALREADY_EXISTS,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL_SERVER_ERROR;

    @Override
    public String getErrorCode() {
        return toString();
    }
}
