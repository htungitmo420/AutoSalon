package org.example.storageservice.infrastructure.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuditWebMvcConfig implements WebMvcConfigurer {

    private final ObjectProvider<AuditLogRecorder> auditLogRecorderProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuditLogRecorder auditLogRecorder = auditLogRecorderProvider.getIfAvailable();
        if (auditLogRecorder != null) {
            registry.addInterceptor(new AuditLogInterceptor(auditLogRecorder)).addPathPatterns("/api/**");
        }
    }
}
