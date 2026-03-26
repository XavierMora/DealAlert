package com.games_price_tracker.api.account.exceptions;

import java.util.concurrent.TimeUnit;

import com.games_price_tracker.api.core.exceptions.TooManyRequestsException;
import com.games_price_tracker.api.core.response.ErrorCode;

public class SignInCodeEmailCooldownException extends TooManyRequestsException {
    public SignInCodeEmailCooldownException(Long retryAfter, TimeUnit timeUnit){
        super(
            retryAfter, 
            timeUnit,
            "Un código fue solicitado recientemente. Intentar más tarde.",
            ErrorCode.EMAIL_COOLDOWN
        );
    }
}
