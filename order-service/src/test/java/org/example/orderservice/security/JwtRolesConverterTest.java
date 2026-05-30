package org.example.orderservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtRolesConverterTest {

    private final JwtRolesConverter converter = new JwtRolesConverter();

    @Test
    void convert_CollectsTopLevelRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .claim("roles", List.of("USER", "MANAGER"))
                .build();

        assertEquals(Set.of("ROLE_USER", "ROLE_MANAGER"), authorities(converter.convert(jwt)));
    }

    @Test
    void convert_ReturnsEmptyAuthoritiesWhenRolesClaimIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        assertEquals(Set.of(), authorities(converter.convert(jwt)));
    }

    private Set<String> authorities(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
