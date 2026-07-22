package com.secondhand.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts exceptions to a simple JSON like:
 * { "status": 400, "message": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", e.getStatus());
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("message", "\u062e\u0637\u0627\u06cc \u062f\u0627\u062e\u0644\u06cc \u0633\u0631\u0648\u0631: " + e.getMessage());
        return ResponseEntity.status(500).body(body);
    }
}
