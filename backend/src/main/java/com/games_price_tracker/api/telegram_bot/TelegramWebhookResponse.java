package com.games_price_tracker.api.telegram_bot;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelegramWebhookResponse {
    private String method;
    private Long chat_id;
    private String text;

    public TelegramWebhookResponse(String method, Long chat_id, String text){
        this.method = method;
        this.chat_id = chat_id;
        this.text = text;
    }

    public String getText() {
        return text;
    }
    
    public Long getChat_id() {
        return chat_id;
    }

    public String getMethod() {
        return method;
    }
}
