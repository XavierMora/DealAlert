package com.games_price_tracker.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;

import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class ExceptionsHandlerController{
    // Controla los argumentos marcados con @Valid que no cumplan con las restricciones 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValid(MethodArgumentNotValidException e){
        HashMap<String, String> errors = new HashMap<String, String>();

        for (FieldError fieldErr : e.getFieldErrors()){
            errors.putIfAbsent(fieldErr.getField(), fieldErr.getDefaultMessage()); // Se queda con el primer error
        }

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> missingRequestHeader(MissingRequestHeaderException e){
        return ResponseEntity.badRequest().body(Map.of("Error", String.format("Se requiere el header %s", e.getHeaderName())));
    }

    @ExceptionHandler(AccountAuthErrorException.class)
    public ResponseEntity<Map<String,String>> accountAuthError(AccountAuthErrorException e){
        return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
    }
}
