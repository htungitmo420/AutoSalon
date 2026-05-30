package org.example.orderservice.security;

import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.example.orderservice.infrastructure.inmemory.InMemoryCommonOrderRepository;
import org.example.orderservice.infrastructure.inmemory.InMemoryCustomOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSecurityEvaluatorTest {

    @Test
    void ownerChecks_ReturnTrueOnlyForOwner() {
        InMemoryCommonOrderRepository commonRepository = new InMemoryCommonOrderRepository();
        InMemoryCustomOrderRepository customRepository = new InMemoryCustomOrderRepository();
        UserProvider userProvider = new UserProvider();
        OrderSecurityEvaluator evaluator = new OrderSecurityEvaluator(commonRepository, customRepository, userProvider);

        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();

        CommonCarOrder commonOrder = commonRepository.save(CommonCarOrder.builder()
                .carId(UUID.randomUUID())
                .customerId(ownerId)
                .status(CommonOrderStatus.CREATED)
                .build());

        CustomCarOrder customOrder = customRepository.save(CustomCarOrder.builder()
                .modelId(UUID.randomUUID())
                .customerId(ownerId)
                .status(CustomOrderStatus.CREATED)
                .build());

        assertTrue(evaluator.isCommonOrderOwner(commonOrder.getId(), authentication(ownerId)));
        assertFalse(evaluator.isCommonOrderOwner(commonOrder.getId(), authentication(strangerId)));
        assertTrue(evaluator.isCustomOrderOwner(customOrder.getId(), authentication(ownerId)));
        assertFalse(evaluator.isCustomOrderOwner(customOrder.getId(), authentication(strangerId)));
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
