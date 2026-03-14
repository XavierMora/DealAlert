package com.games_price_tracker.api.tracking.enqueue_games.enums;

import com.games_price_tracker.api.common.response.ApiError;

public enum StartEnqueueResult implements ApiError{
    STARTED,
    ENQUEUE_ALREADY_SCHEDULED;

    @Override
    public String getErrorCode() {
        return toString();
    }
}
