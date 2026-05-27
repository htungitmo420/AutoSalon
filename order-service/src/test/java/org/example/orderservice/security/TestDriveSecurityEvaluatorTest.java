package org.example.orderservice.security;

import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.example.orderservice.infrastructure.inmemory.InMemoryTestDriveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDriveSecurityEvaluatorTest {

    @Test
    void ownerCheck_ReturnTrueOnlyForOwner() {
        InMemoryTestDriveRepository repository = new InMemoryTestDriveRepository();
        TestDriveSecurityEvaluator evaluator = new TestDriveSecurityEvaluator(repository, new UserProvider());

        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        TestDrive testDrive = repository.save(TestDrive.builder()
                .carId(UUID.randomUUID())
                .customerId(ownerId)
                .status(TestDriveStatus.PENDING)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .build());

        assertTrue(evaluator.isOwner(testDrive.getId(), authentication(ownerId)));
        assertFalse(evaluator.isOwner(testDrive.getId(), authentication(strangerId)));
    }

    private UsernamePasswordAuthenticationToken authentication(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .build();

        return new UsernamePasswordAuthenticationToken(jwt, jwt, List.of());
    }
}