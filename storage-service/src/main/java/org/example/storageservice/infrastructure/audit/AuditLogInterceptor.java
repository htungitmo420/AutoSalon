package org.example.storageservice.infrastructure.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditLogRecorder auditLogRecorder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           org.springframework.web.servlet.ModelAndView modelAndView) {
        if (!MUTATING_METHODS.contains(request.getMethod()) || response.getStatus() >= 400) {
            return;
        }
        try {
            auditLogRecorder.record(request.getMethod(), request.getRequestURI());
        } catch (RuntimeException exception) {
            log.error("Unable to persist audit log for {} {}", request.getMethod(), request.getRequestURI(),
                    exception);
        }
    }
}
