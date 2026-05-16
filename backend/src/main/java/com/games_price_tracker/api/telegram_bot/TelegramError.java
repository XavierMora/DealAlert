package com.games_price_tracker.api.telegram_bot;

import com.games_price_tracker.api.core.response.ApiError;

public enum TelegramError implements ApiError {
    TELEGRAM_ALREADY_LINKED,
    TELEGRAM_TOKEN_CREATION_FAILED;
}
