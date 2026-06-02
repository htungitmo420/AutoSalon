package org.example.paymentservice.security;

import org.example.paymentservice.application.port.CurrentUserProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserProvider implements CurrentUserProvider {
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return parse(jwt);
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof Jwt jwt) {
            return parse(jwt);
        }
        throw new AccessDeniedException("JWT principal is required");
    }

    private UUID parse(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (RuntimeException exception) {
            throw new AccessDeniedException("JWT subject must be a UUID");
        }
    }
}
