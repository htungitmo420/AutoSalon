package org.example.orderservice.security;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("testDriveSecurityEvaluator")
@RequiredArgsConstructor
public class TestDriveSecurityEvaluator {

    private final TestDriveRepository testDriveRepository;
    private final UserProvider userProvider;

    public boolean isOwner(UUID testDriveId, Authentication authentication) {
        UUID currentUserId = userProvider.extractUserId(authentication);
        return testDriveRepository.existsByIdAndCustomerId(testDriveId, currentUserId);
    }
}
