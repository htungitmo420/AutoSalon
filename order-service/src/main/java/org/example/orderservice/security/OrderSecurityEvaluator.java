package org.example.orderservice.security;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orderSecurityEvaluator")
@RequiredArgsConstructor
public class OrderSecurityEvaluator {

    private final CommonOrderRepository commonOrderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final UserProvider userProvider;

    public boolean isCommonOrderOwner(UUID orderId, Authentication authentication) {
        UUID currentUserId = userProvider.extractUserId(authentication);
        return commonOrderRepository.existsByIdAndCustomerId(orderId, currentUserId);
    }

    public boolean isCustomOrderOwner(UUID orderId, Authentication authentication) {
         UUID currentUserId = userProvider.extractUserId(authentication);
         return customOrderRepository.existsByIdAndCustomerId(orderId, currentUserId);
    }
}
