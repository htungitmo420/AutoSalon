package org.example.orderservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeycloakJwtRolesConverterTest {

    private final KeycloakJwtRolesConverter converter = new KeycloakJwtRolesConverter("auto-salon-api");

    @Test
    void convert_CollectsRealmAndClientRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .claim("realm_access", Map.of("roles", List.of("USER", "MANAGER")))
                .claim("resource_access", Map.of(
                        "auto-salon-api", Map.of("roles", List.of("WAREHOUSE_ADMIN", "ADMIN"))
                ))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(
                Set.of("ROLE_USER", "ROLE_MANAGER", "ROLE_WAREHOUSE_ADMIN", "ROLE_ADMIN"),
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
        );
    }
}