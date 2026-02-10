package com.games_price_tracker.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"success", "message", "data", "error"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBody<T>{
    private boolean success;
    private String message;
    private T data; 
    private String error;

    public ApiResponseBody(){}

    public ApiResponseBody(boolean success, String message, T data, ApiError error){
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error == null ? null : error.getErrorCode();
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean getSuccess(){
        return success;
    }

    public String getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error == null ? null : error.getErrorCode();
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

}
