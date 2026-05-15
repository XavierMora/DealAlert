package com.games_price_tracker.api.account.exceptions;

import com.games_price_tracker.api.core.response.ApiError;

public enum AuthError implements ApiError{
    INVALID_CODE,
    MAX_ATTEMPTS_REACHED,
    EMAIL_NOT_FOUND;
}
