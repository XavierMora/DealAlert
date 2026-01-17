package com.games_price_tracker.api.security;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationTokenException extends AuthenticationException{

    public AuthenticationTokenException(String msg) {
        super(msg);
    }
}
