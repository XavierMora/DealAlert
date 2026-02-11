package com.games_price_tracker.api.common.exceptions;

import java.util.concurrent.TimeUnit;

import com.games_price_tracker.api.common.response.ApiError;
import com.games_price_tracker.api.common.response.ErrorCode;

public class TooManyRequestsException extends RuntimeException{
    private Long retryAfterSeconds;
    private ApiError errorCode;

    public TooManyRequestsException(){
        super("Muchas peticiones. Intentar más tarde.");
        this.errorCode = ErrorCode.TOO_MANY_REQUESTS;
    }

    public TooManyRequestsException(Long retryAfter, TimeUnit timeUnit){
        this();
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
    }

    public TooManyRequestsException(Long retryAfter, TimeUnit timeUnit, String msg){
        super(msg);
        this.retryAfterSeconds = timeUnit.toSeconds(retryAfter);
        this.errorCode = ErrorCode.TOO_MANY_REQUESTS;
    }

    public TooManyRequestsException(Long retryAfter, TimeUnit timeUnit, String msg, ApiError errorCode){
        this(retryAfter, timeUnit, msg);
        this.errorCode = errorCode;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public ApiError getErrorCode(){
        return errorCode;
    }
}
