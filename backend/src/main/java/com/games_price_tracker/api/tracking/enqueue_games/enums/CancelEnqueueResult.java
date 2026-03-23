package com.games_price_tracker.api.tracking.enqueue_games.enums;

import com.games_price_tracker.api.core.response.ApiError;

public enum CancelEnqueueResult implements ApiError{
    NO_ENQUEUE_SCHEDULED,
    CANCEL_FAILED,
    CANCELED;

    @Override
    public String getErrorCode() {
        return toString();
    }
}
