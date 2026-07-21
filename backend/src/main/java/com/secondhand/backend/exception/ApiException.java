package com.secondhand.backend.exception;

/**
 * A simple exception that carries an HTTP status code and a message.
 * The services throw this exception and the GlobalExceptionHandler
 * converts it to a JSON error response.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
