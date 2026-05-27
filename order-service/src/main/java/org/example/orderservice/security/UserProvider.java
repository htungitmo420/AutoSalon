package org.example.orderservice.security;

import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserProvider implements CurrentUserProvider {

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return extractUserId(authentication);
    }

    public UUID extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authenticated user is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractUserId(jwt);
        }

        Object credentials = authentication.getCredentials();
        if (credentials instanceof Jwt jwt) {
            return extractUserId(jwt);
        }

        throw new AccessDeniedException("JWT principal is required");
    }

    public boolean hasAnyRole(SecurityRoles... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return hasAnyRole(authentication, roles);
    }

    @Override
    public boolean hasElevatedReadAccess() {
        return hasAnyRole(SecurityRoles.MANAGER, SecurityRoles.ADMIN);
    }

    private boolean hasAnyRole(Authentication authentication, SecurityRoles... roles) {
        if (authentication == null) {
            return false;
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Arrays.stream(roles)
                .map(role -> "ROLE_" + role.name())
                .anyMatch(authorities::contains);
    }

    private UUID extractUserId(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new AccessDeniedException("JWT subject is missing");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("JWT subject must be a UUID");
        }
    }
}
