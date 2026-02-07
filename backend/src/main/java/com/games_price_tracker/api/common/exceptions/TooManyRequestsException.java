package com.games_price_tracker.api.common.exceptions;

import java.util.concurrent.TimeUnit;

public class TooManyRequestsException extends RuntimeException{
    private long retryAfterSeconds;

    public TooManyRequestsException(long retryAfter, TimeUnit timeUnit){
        super("Muchas peticiones. Intentar más tarde.");
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
    }

    public TooManyRequestsException(long retryAfter, TimeUnit timeUnit, String msg){
        super(msg);
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
