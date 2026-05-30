package org.example.orderservice.security;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.out.JwtTokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenService implements JwtTokenIssuer {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer:auto-salon}")
    private String issuer;

    @Value("${security.jwt.access-token-ttl-seconds:3600}")
    private long accessTokenTtlSeconds;

    @Value("${security.jwt.audience:auto-salon-api}")
    private String audience;

    @Override
    public String issueToken(UUID userId, String email, Set<String> roles) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(accessTokenTtlSeconds))
                .subject(userId.toString())
                .audience(List.of(audience))
                .claim("email", email)
                .claim("roles", roles.stream().sorted().toList())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long expiresInSeconds() {
        return accessTokenTtlSeconds;
    }
}
