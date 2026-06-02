package org.example.cartservice.domain.exceptions;

public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
