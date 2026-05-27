package org.example.storageservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KeycloakJwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    public KeycloakJwtRolesConverter(@Value("${security.keycloak.client-id:auto-salon-api}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        var realmRoles = extractRealmRoles(jwt);
        var clientRoles = extractClientRoles(jwt);

        return Stream.concat(realmRoles.stream(), clientRoles.stream())
                .map(this::toAuthority)
                .collect(Collectors.toSet());
    }

    private List<String> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }
        return extractRoles(realmAccessMap);
    }

    private List<String> extractClientRoles(Jwt jwt) {
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap)) {
            return List.of();
        }

        Object clientAccess = resourceAccessMap.get(clientId);
        if (!(clientAccess instanceof Map<?, ?> clientAccessMap)) {
            return List.of();
        }
        return extractRoles(clientAccessMap);
    }

    private List<String> extractRoles(Map<?, ?> accessMap) {
        Object roles = accessMap.get("roles");
        if (!(roles instanceof Collection<?> collection)) {
            return List.of();
        }

        return collection.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }

    private GrantedAuthority toAuthority(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role);
    }
}