package org.example.orderservice.presentation.exception;

import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.application.exception.InvalidAuthTokenException;
import org.example.orderservice.application.exception.InvalidCredentialsException;
import org.example.orderservice.application.exception.RateLimitExceededException;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "Entity not found", ex.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Domain validation failed", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Bad request", ex.getMessage());
    }

    @ExceptionHandler(StorageServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleStorageServiceUnavailable(StorageServiceUnavailableException ex) {
        return buildProblem(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE",
                "Storage service unavailable", ex.getMessage());
    }

    @ExceptionHandler({InvalidAuthTokenException.class, InvalidCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication failed", ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildProblem(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many requests", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .toList();
        return buildProblem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
                "Request validation failed", fieldErrors);
    }

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String code, String title, String detail) {
        return buildProblem(status, code, title, detail, List.of());
    }

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String code, String title, String detail,
                                                       List<Map<String, String>> fieldErrors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("message", detail);
        problem.setProperty("traceId", TraceContext.currentTraceId());
        problem.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.status(status).body(problem);
    }
}
