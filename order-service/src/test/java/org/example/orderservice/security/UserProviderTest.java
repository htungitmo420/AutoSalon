package org.example.orderservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserProviderTest {

    private final UserProvider userProvider = new UserProvider();

    @Test
    void extractUserId_ReturnsUuidFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken authentication = authentication(userId);

        assertEquals(userId, userProvider.extractUserId(authentication));
    }

    @Test
    void extractUserId_InvalidSubject_ThrowsAccessDenied() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("not-a-uuid")
                .claim("roles", List.of("USER"))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwt, jwt, List.of());

        assertThrows(AccessDeniedException.class, () -> userProvider.extractUserId(authentication));
    }

    private UsernamePasswordAuthenticationToken authentication(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .build();

        return new UsernamePasswordAuthenticationToken(jwt, jwt, List.of());
    }
}
