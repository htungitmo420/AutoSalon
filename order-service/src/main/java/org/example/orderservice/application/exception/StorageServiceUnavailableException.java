package org.example.orderservice.application.exception;

public class StorageServiceUnavailableException extends RuntimeException {

    public StorageServiceUnavailableException(String message) {
        super(message);
    }

    public StorageServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
