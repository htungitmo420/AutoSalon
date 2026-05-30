package org.example.orderservice.presentation.exception;

import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.application.exception.InvalidAuthTokenException;
import org.example.orderservice.application.exception.InvalidCredentialsException;
import org.example.orderservice.application.exception.RateLimitExceededException;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, "Entity not found", ex.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "Domain validation failed", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage());
    }

    @ExceptionHandler(StorageServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleStorageServiceUnavailable(StorageServiceUnavailableException ex) {
        return buildProblem(HttpStatus.SERVICE_UNAVAILABLE, "Storage service unavailable", ex.getMessage());
    }

    @ExceptionHandler({InvalidAuthTokenException.class, InvalidCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildProblem(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", ex.getMessage());
    }

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return ResponseEntity.status(status).body(problem);
    }
}
