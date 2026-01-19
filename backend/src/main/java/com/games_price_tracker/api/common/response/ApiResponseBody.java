package com.games_price_tracker.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBody{
    private String message;
    private Object data; 

    public ApiResponseBody(){}

    public ApiResponseBody(String message, Object data){
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
