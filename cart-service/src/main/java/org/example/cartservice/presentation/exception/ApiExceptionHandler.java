package org.example.cartservice.presentation.exception;

import org.example.cartservice.domain.exceptions.DomainValidationException;
import org.example.cartservice.domain.exceptions.EntityNotFoundException;
import org.example.cartservice.infrastructure.logging.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException exception) {
        return buildProblem(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "Entity not found", exception.getMessage());
    }

    @ExceptionHandler({DomainValidationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        return buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return buildProblem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", "Access denied");
    }

    private ProblemDetail buildProblem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("message", detail);
        problem.setProperty("traceId", TraceContext.currentTraceId());
        problem.setProperty("fieldErrors", List.of());
        return problem;
    }
}
