package com.games_price_tracker.api.account.enums;

public enum SignInCodeResult {
    SUCCESS("Se envió un codigo al email para iniciar sesión."),
    TOO_MANY_REQUESTS("Un código fue enviado recientemente. Intentar más tarde.");

    private String msg;

    private SignInCodeResult(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }
}
