package com.games_price_tracker.api.core.exceptions;

import com.games_price_tracker.api.core.response.ApiError;
import com.games_price_tracker.api.core.response.ErrorCode;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg){
        super(msg);
    }

    public ApiError getErrorCode(){
        return ErrorCode.RESOURCE_NOT_FOUND;
    }
}
