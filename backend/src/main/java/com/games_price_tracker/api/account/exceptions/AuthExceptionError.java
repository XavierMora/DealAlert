package com.games_price_tracker.api.account.exceptions;

import com.games_price_tracker.api.common.response.ApiError;

public enum AuthExceptionError implements ApiError{
    EXPIRED_CODE,
    INCORRECT_CODE;

    @Override
    public String getError() {
        return toString();
    }
}
