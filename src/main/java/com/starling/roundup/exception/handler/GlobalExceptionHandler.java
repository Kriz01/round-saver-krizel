package com.starling.roundup.exception.handler;

import com.starling.roundup.exception.ApiException;
import com.starling.roundup.exception.CurrencyNotFoundException;
import com.starling.roundup.exception.FeedNotFoundException;
import com.starling.roundup.exception.MultipleCurrenciesFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> buildErrorResponse(Exception ex, HttpStatusCode status) {
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipleCurrenciesFoundException.class)
    public ResponseEntity<Map<String, String>> handleMultipleCurrencies(MultipleCurrenciesFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException ex) {
        return buildErrorResponse(ex, ex.getStatus());
    }

    @ExceptionHandler(FeedNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFeedNotFound(FeedNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

}
