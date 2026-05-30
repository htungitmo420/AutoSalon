package org.example.orderservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientAddressResolver {

    @Value("${security.auth.trusted-proxy.use-forwarded-for:false}")
    private boolean useForwardedFor;

    public String resolve(HttpServletRequest request) {
        if (useForwardedFor) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
