package org.example.orderservice.presentation.exception;

public class IncompatibleComponentException extends RuntimeException {
    public IncompatibleComponentException(String message) {
        super(message);
    }

    public IncompatibleComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}