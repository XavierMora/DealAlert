package com.games_price_tracker.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.game.exceptions.GameNotFoundException;

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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, List<String>>> methodArgumentNotValid(HandlerMethodValidationException e){
        List<String> errors = new LinkedList<String>();

        for (MessageSourceResolvable msgSourceResolvable : e.getAllErrors()) {
            errors.add(msgSourceResolvable.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(Map.of("error", errors));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> missingRequestHeader(MissingRequestHeaderException e){
        return ResponseEntity.badRequest().body(Map.of("error", String.format("Se requiere el header %s", e.getHeaderName())));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<Map<String, String>> missingRequestCookie(MissingRequestCookieException e){
        return ResponseEntity.badRequest().body(Map.of("error", String.format("Se requiere la cookie %s", e.getCookieName())));
    }

    @ExceptionHandler(AccountAuthErrorException.class)
    public ResponseEntity<Map<String,String>> accountAuthError(AccountAuthErrorException e){
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Map<String,String>> gameNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Juego no encontrado"));
    }
}
