package com.games_price_tracker.api.core.response;

public interface ApiError {
    default String getErrorCode(){
        return toString();
    };
}
