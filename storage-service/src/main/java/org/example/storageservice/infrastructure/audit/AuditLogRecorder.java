package org.example.storageservice.infrastructure.audit;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.infrastructure.logging.TraceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogRecorder {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resource) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? "anonymous"
                : authentication.getName();
        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .action(action)
                .resource(resource)
                .traceId(TraceContext.currentTraceId())
                .occurredAt(Instant.now())
                .build());
    }
}
