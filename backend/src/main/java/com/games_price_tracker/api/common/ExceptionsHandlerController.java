package com.games_price_tracker.api.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.games_price_tracker.api.account.exceptions.AccountAuthErrorException;
import com.games_price_tracker.api.common.response.ApiResponseBody;
import com.games_price_tracker.api.email.SendEmailException;
import com.games_price_tracker.api.game.exceptions.GameNotFoundException;

import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class ExceptionsHandlerController{
    // Controla los argumentos marcados con @Valid que no cumplan con las restricciones 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseBody> methodArgumentNotValid(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<String, String>();
        
        for (FieldError fieldErr : e.getFieldErrors()){
            errors.putIfAbsent(fieldErr.getField(), fieldErr.getDefaultMessage()); // Se queda con el primer error
        }
        
        return ResponseEntity.badRequest().body(new ApiResponseBody(
            "Datos inválidos.",
            errors
        ));
    }
    
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponseBody> handlerMethodValidation(HandlerMethodValidationException e){
        Map<String, String> errors = new HashMap<String, String>();
        
        for (ParameterValidationResult err : e.getValueResults()) {            
            for (MessageSourceResolvable msg : err.getResolvableErrors()) {
                errors.putIfAbsent(err.getMethodParameter().getParameter().getName(), msg.getDefaultMessage());
                break;
            }
        }
        
        return ResponseEntity.badRequest().body(new ApiResponseBody(
            "Datos inválidos.",
            errors
        ));
    }

    @ExceptionHandler(MissingRequestValueException.class) // Excepciones cuando falta un header,cookie,path variable
    public ResponseEntity<ApiResponseBody> missingRequestHeader(MissingRequestValueException e){
        ApiResponseBody body = new ApiResponseBody("Faltan datos requeridos.", null);
        
        if(e instanceof MissingRequestHeaderException){
            body.setData(Map.of("header", ((MissingRequestHeaderException) e).getHeaderName()));
        }else if(e instanceof MissingRequestCookieException){
            body.setData(Map.of("cookie", ((MissingRequestCookieException) e).getCookieName()));
        }else if(e instanceof MissingPathVariableException){
            body.setData(Map.of("path_variable", ((MissingPathVariableException) e).getParameter().getParameterName()));
        }else if(e instanceof MissingServletRequestParameterException){
            body.setData(Map.of("parameter", ((MissingServletRequestParameterException) e).getParameterName()));
        }

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccountAuthErrorException.class)
    public ResponseEntity<ApiResponseBody> accountAuthError(AccountAuthErrorException e){
        return ResponseEntity.badRequest().body(new ApiResponseBody(
            e.getMessage(),
            null
        ));
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ApiResponseBody> gameNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseBody(
            "Juego no encontrado.",
            null
        ));
    }

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<ApiResponseBody> sendEmail(SendEmailException e){
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiResponseBody(
            e.getMessage(), 
            null
        ));
    }
}
