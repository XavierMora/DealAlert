package com.games_price_tracker.api.account.exceptions;

import com.games_price_tracker.api.core.response.ApiError;

public enum AuthError implements ApiError{
    EXPIRED_CODE,
    INCORRECT_CODE,
    CODE_SENT_RECENTLY,
    MAX_ATTEMPTS_REACHED,
    EMAIL_NOT_FOUND;
    
    @Override
    public String getErrorCode() {
        return toString();
    }
}
