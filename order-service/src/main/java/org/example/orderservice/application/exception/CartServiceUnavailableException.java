package org.example.orderservice.application.exception;

public class CartServiceUnavailableException extends RuntimeException {

    public CartServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
