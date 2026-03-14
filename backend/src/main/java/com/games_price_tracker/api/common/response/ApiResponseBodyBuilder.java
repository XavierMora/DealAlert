package com.games_price_tracker.api.common.response;

public class ApiResponseBodyBuilder {
    public static <T> ApiResponseBody<T> success(String message, T data){
        return new ApiResponseBody<T>(true, message, data, null);
    }

    public static <T> ApiResponseBody<T> success(String message){
        return new ApiResponseBody<T>(true, message, null, null);
    }

    public static <T> ApiResponseBody<T> success(T data){
        return new ApiResponseBody<T>(true, null, data, null);
    }

    public static <T> ApiResponseBody<T> error(String message, T data, ApiError error){
        return new ApiResponseBody<T>(false, message, data, error);
    }

    public static <T> ApiResponseBody<T> error(String message, ApiError error){
        return new ApiResponseBody<T>(false, message, null, error);
    }

    public static <T> ApiResponseBody<T> error(ApiError error){
        return new ApiResponseBody<T>(false, null, null, error);
    }
}
