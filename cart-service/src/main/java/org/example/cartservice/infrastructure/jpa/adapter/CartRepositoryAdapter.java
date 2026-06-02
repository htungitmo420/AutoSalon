package org.example.cartservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.application.repository.CartRepository;
import org.example.cartservice.domain.cart.enums.CartStatus;
import org.example.cartservice.domain.cart.model.Cart;
import org.example.cartservice.infrastructure.jpa.repository.JpaCartRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final JpaCartRepository delegate;

    @Override
    public Cart save(Cart cart) {
        return delegate.save(cart);
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public Optional<Cart> findActiveByCustomerId(UUID customerId) {
        return delegate.findFirstByCustomerIdAndStatusAndRemovedFalse(customerId, CartStatus.ACTIVE);
    }

    @Override
    public List<Cart> findActiveExpiredBefore(Instant threshold) {
        return delegate.findAllByStatusAndQuoteExpiresAtBeforeAndRemovedFalse(CartStatus.ACTIVE, threshold);
    }
}
