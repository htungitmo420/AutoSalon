package org.example.paymentservice.presentation;

import org.example.paymentservice.domain.exceptions.DomainValidationException;
import org.example.paymentservice.domain.exceptions.EntityNotFoundException;
import org.example.paymentservice.infrastructure.logging.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    ProblemDetail notFound(EntityNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler({DomainValidationException.class, IllegalArgumentException.class})
    ProblemDetail badRequest(RuntimeException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(detail);
        problem.setProperty("code", code);
        problem.setProperty("message", detail);
        problem.setProperty("traceId", TraceContext.currentTraceId());
        problem.setProperty("fieldErrors", List.of());
        return problem;
    }
}
