package org.example.storageservice.presentation.exception;

import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.exceptions.IncompatibleComponentException;
import org.example.storageservice.infrastructure.logging.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "Entity not found", ex.getMessage());
    }

    @ExceptionHandler(IncompatibleComponentException.class)
    public ProblemDetail handleIncompatibleComponent(IncompatibleComponentException ex) {
        return buildProblem(HttpStatus.CONFLICT, "INCOMPATIBLE_COMPONENT", "Incompatible component", ex.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Domain validation failed", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Bad request", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .toList();
        return buildProblem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
                "Request validation failed", fieldErrors);
    }

    private ProblemDetail buildProblem(HttpStatus status, String code, String title, String detail) {
        return buildProblem(status, code, title, detail, List.of());
    }

    private ProblemDetail buildProblem(HttpStatus status, String code, String title, String detail,
                                       List<Map<String, String>> fieldErrors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("message", detail);
        problem.setProperty("traceId", TraceContext.currentTraceId());
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }
}
