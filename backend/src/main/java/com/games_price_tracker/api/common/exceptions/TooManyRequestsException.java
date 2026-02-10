package com.games_price_tracker.api.common.exceptions;

import java.util.concurrent.TimeUnit;

import com.games_price_tracker.api.common.response.ApiError;
import com.games_price_tracker.api.common.response.ErrorCode;

public class TooManyRequestsException extends RuntimeException{
    private long retryAfterSeconds;
    private ApiError errorCode;

    public TooManyRequestsException(long retryAfter, TimeUnit timeUnit){
        super("Muchas peticiones. Intentar más tarde.");
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
        this.errorCode = ErrorCode.TOO_MANY_REQUESTS;
    }

    public TooManyRequestsException(long retryAfter, TimeUnit timeUnit, String msg){
        super(msg);
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
    }

    public TooManyRequestsException(long retryAfter, TimeUnit timeUnit, String msg, ApiError errorCode){
        this(retryAfter, timeUnit, msg);
        this.errorCode = errorCode;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public ApiError getErrorCode(){
        return errorCode;
    }
}
