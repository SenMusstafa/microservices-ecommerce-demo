package com.mustafasen.orderservice.errors;

import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // inventory-service unreachable (connection refused/timeout) or not registered in Eureka.
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<Map<String, String>> handleUnreachable(RetryableException ex) {
        return unavailable();
    }

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    public ResponseEntity<Map<String, String>> handleNoInstance(FeignException.ServiceUnavailable ex) {
        return unavailable();
    }

    private ResponseEntity<Map<String, String>> unavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "inventory-service is unavailable, try again later"));
    }
}
