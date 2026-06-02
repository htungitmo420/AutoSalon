package org.example.cartservice.infrastructure.jpa.repository;

import org.example.cartservice.domain.cart.enums.CartStatus;
import org.example.cartservice.domain.cart.model.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "selectedPartIds")
    Optional<Cart> findByIdAndRemovedFalse(UUID id);

    @EntityGraph(attributePaths = "selectedPartIds")
    Optional<Cart> findFirstByCustomerIdAndStatusAndRemovedFalse(UUID customerId, CartStatus status);

    List<Cart> findAllByStatusAndQuoteExpiresAtBeforeAndRemovedFalse(CartStatus status, Instant threshold);
}
