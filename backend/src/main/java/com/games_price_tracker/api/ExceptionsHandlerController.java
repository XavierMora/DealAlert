package com.games_price_tracker.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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
}
