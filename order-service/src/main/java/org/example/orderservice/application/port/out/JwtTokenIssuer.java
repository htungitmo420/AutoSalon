package org.example.orderservice.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface JwtTokenIssuer {

    String issueToken(UUID userId, String email, Set<String> roles);

    long expiresInSeconds();
}
