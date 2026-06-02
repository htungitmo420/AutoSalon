package org.example.cartservice.application.repository;

import org.example.cartservice.domain.cart.model.Cart;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID id);

    Optional<Cart> findActiveByCustomerId(UUID customerId);

    List<Cart> findActiveExpiredBefore(Instant threshold);
}
