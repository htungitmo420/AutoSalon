package org.example.notificationservice.presentation;

import org.example.notificationservice.domain.exception.EntityNotFoundException;
import org.example.notificationservice.infrastructure.logging.TraceContext;
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
        return problem(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "Entity not found", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", "Access denied");
    }

    private ProblemDetail problem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("message", detail);
        problem.setProperty("traceId", TraceContext.currentTraceId());
        problem.setProperty("fieldErrors", List.of());
        return problem;
    }
}
