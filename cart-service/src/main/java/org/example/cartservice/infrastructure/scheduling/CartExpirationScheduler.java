package org.example.cartservice.infrastructure.scheduling;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.application.service.CartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartExpirationScheduler {

    private final CartService cartService;

    @Scheduled(fixedDelayString = "${cart.expiration.fixed-delay-ms:60000}")
    public void expireActiveCarts() {
        cartService.expireActiveCarts();
    }
}
