package com.secondhand.frontend;

/**
 * Error coming from the backend (or a connection problem).
 * The message is already user friendly and can be shown in an Alert.
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
