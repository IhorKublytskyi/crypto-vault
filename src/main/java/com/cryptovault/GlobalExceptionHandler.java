package com.cryptovault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private Map<String, Object> createErrorBody(HttpStatus status, String error, String message, WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return body;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleExceptions(Exception ex, WebRequest request) {
        String userMessage = "An unexpected internal error occurred.";

        return new ResponseEntity<>(
                createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", userMessage, request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
