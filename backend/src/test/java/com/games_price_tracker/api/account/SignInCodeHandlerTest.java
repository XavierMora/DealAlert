package com.games_price_tracker.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.games_price_tracker.api.account.sign_in_code.SignInCodeHandler;
import com.games_price_tracker.api.core.TokenGenerator;

public class SignInCodeHandlerTest {
    private SignInCodeHandler signInCodeHandler;

    @BeforeEach
    void setup(){
        TokenGenerator<String> tokenGenerator = new TokenGenerator<String>() {
            int option = 1;
            @Override
            public String generate() {
                String token;
                if(option == 1){
                    option = 0;
                    token = "123456";
                }else{
                    option = 1;    
                    token = "654321";
                }
                return token;
            }
        };

        signInCodeHandler = new SignInCodeHandler(Duration.ofMinutes(2), tokenGenerator);
    }

    @Test
    void shouldCreateAndGetCode(){
        assertEquals("123456", signInCodeHandler.getOrCreate("email"));
    }

    @Test
    void shouldOnlyGetAndNotCreateACode(){
        signInCodeHandler.getOrCreate("email1");
        assertEquals("654321", signInCodeHandler.getOrCreate("email2"));
        assertEquals("123456", signInCodeHandler.getOrCreate("email1"));
    }

    @Test
    void shouldReturnValidCode(){
        signInCodeHandler.getOrCreate("email");
        assertTrue(signInCodeHandler.codeIsValid("email", "123456"));
    }

    @Test
    void shouldReturnInvalidCode(){
        signInCodeHandler.getOrCreate("email");
        assertFalse(signInCodeHandler.codeIsValid("email", "000000"));
        assertFalse(signInCodeHandler.codeIsValid("invalidEmail", "")); // email que no existe, no solicitó código
    }
}
