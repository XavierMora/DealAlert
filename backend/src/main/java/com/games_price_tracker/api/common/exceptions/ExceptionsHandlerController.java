package com.games_price_tracker.api.common.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
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
import com.games_price_tracker.api.common.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.common.response.ErrorCode;
import com.games_price_tracker.api.email.SendEmailException;

import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class ExceptionsHandlerController{
    // Controla los argumentos marcados con @Valid que no cumplan con las restricciones 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseBody<Map<String, String>>> methodArgumentNotValid(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<String, String>();
        
        for (FieldError fieldErr : e.getFieldErrors()){
            errors.putIfAbsent(fieldErr.getField(), fieldErr.getDefaultMessage()); // Se queda con el primer error
        }
        
        return ResponseEntity.badRequest().body(
            ApiResponseBodyBuilder.error("Datos inválidos", errors, ErrorCode.INVALID_DATA)
        );
    }
    
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponseBody<Map<String, String>>> handlerMethodValidation(HandlerMethodValidationException e){
        Map<String, String> errors = new HashMap<String, String>();
        
        // Errores de parametros que tienen constraints
        for (ParameterValidationResult err : e.getValueResults()) {    
            for (MessageSourceResolvable msg : err.getResolvableErrors()) {
                errors.putIfAbsent(err.getMethodParameter().getParameter().getName(), msg.getDefaultMessage());
                break;
            }
        }

        // Errores de parametros con @valid, es decir, son objetos que sus atributos tienen constraints
        for (ParameterErrors err : e.getBeanResults()) {
            for (FieldError fieldErr : err.getFieldErrors()) {
                errors.put(
                    fieldErr.getField(),
                    fieldErr.getDefaultMessage()
                );
            }
        }
        
        return ResponseEntity.badRequest().body(
            ApiResponseBodyBuilder.error("Datos inválidos.", errors, ErrorCode.INVALID_DATA)
        );
    }

    @ExceptionHandler(MissingRequestValueException.class) // Excepciones cuando falta un header,cookie,path variable
    public ResponseEntity<ApiResponseBody<Map<String, String>>> missingRequestHeader(MissingRequestValueException e){
        ApiResponseBody<Map<String, String>> body = ApiResponseBodyBuilder.error("Faltan datos requeridos.", ErrorCode.INVALID_DATA);
        
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
    public ResponseEntity<ApiResponseBody<Void>> accountAuthError(AccountAuthErrorException e){
        return ResponseEntity.badRequest().body(ApiResponseBodyBuilder.error(
            e.getMessage(),
            e.getErrorCode()
        ));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponseBody<Void>> tooManyRequests(TooManyRequestsException e){
        BodyBuilder bodyBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", String.valueOf(e.getRetryAfterSeconds()));

        return bodyBuilder.body(ApiResponseBodyBuilder.error(e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<ApiResponseBody<Void>> sendEmail(SendEmailException e){
        return ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .body(ApiResponseBodyBuilder.error(
            e.getMessage(), 
            ErrorCode.SENDING_EMAIL
        ));
    }
}
