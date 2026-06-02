package org.example.cartservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.application.dto.request.SaveCartRequest;
import org.example.cartservice.application.dto.response.CartResponse;
import org.example.cartservice.application.mapper.CartMapper;
import org.example.cartservice.application.port.out.CartExpiryIndex;
import org.example.cartservice.application.port.out.CurrentUserProvider;
import org.example.cartservice.application.repository.CartRepository;
import org.example.cartservice.domain.cart.enums.CartItemType;
import org.example.cartservice.domain.cart.enums.CartStatus;
import org.example.cartservice.domain.cart.model.Cart;
import org.example.cartservice.domain.exceptions.DomainValidationException;
import org.example.cartservice.domain.exceptions.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CartExpiryIndex cartExpiryIndex;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CartResponse saveCurrentCart(SaveCartRequest request) {
        UUID customerId = currentUserProvider.getCurrentUserId();
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseGet(() -> Cart.builder()
                        .customerId(customerId)
                        .status(CartStatus.ACTIVE)
                        .build());
        apply(cart, request);
        return toTrackedResponse(cartRepository.save(cart));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CartResponse getCurrentCart() {
        UUID customerId = currentUserProvider.getCurrentUserId();
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Active cart not found"));
        expireIfNeeded(cart);
        return CartMapper.INSTANCE.toResponse(cart);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CartResponse getCart(UUID cartId) {
        Cart cart = ownedCart(cartId, currentUserProvider.getCurrentUserId());
        expireIfNeeded(cart);
        return CartMapper.INSTANCE.toResponse(cart);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void clearCart(UUID cartId) {
        Cart cart = ownedCart(cartId, currentUserProvider.getCurrentUserId());
        validateStatus(cart, CartStatus.ACTIVE, "Only an active cart can be cleared");
        cart.setRemoved(true);
        cartRepository.save(cart);
        cartExpiryIndex.remove(cartId);
    }

    @Transactional
    public Cart lockCart(UUID cartId, UUID customerId) {
        Cart cart = ownedCart(cartId, customerId);
        if (cart.getStatus() == CartStatus.LOCKED || cart.getStatus() == CartStatus.CHECKED_OUT) {
            return cart;
        }
        validateStatus(cart, CartStatus.ACTIVE, "Only an active cart can be locked");
        expireIfNeeded(cart);
        cart.setStatus(CartStatus.LOCKED);
        Cart saved = cartRepository.save(cart);
        cartExpiryIndex.remove(cartId);
        return saved;
    }

    @Transactional
    public Cart markCheckedOut(UUID cartId, UUID customerId) {
        Cart cart = ownedCart(cartId, customerId);
        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            return cart;
        }
        validateStatus(cart, CartStatus.LOCKED, "Only a locked cart can be checked out");
        cart.setStatus(CartStatus.CHECKED_OUT);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart releaseCart(UUID cartId, UUID customerId) {
        Cart cart = ownedCart(cartId, customerId);
        if (cart.getStatus() != CartStatus.LOCKED) {
            return cart;
        }
        cart.setStatus(CartStatus.ACTIVE);
        return tracked(cartRepository.save(cart));
    }

    @Transactional
    public void expireActiveCarts() {
        cartRepository.findActiveExpiredBefore(Instant.now()).forEach(cart -> {
            cart.setStatus(CartStatus.EXPIRED);
            cartRepository.save(cart);
            cartExpiryIndex.remove(cart.getId());
        });
    }

    private void apply(Cart cart, SaveCartRequest request) {
        validateRequest(request);
        cart.setItemType(request.itemType());
        cart.setCarId(request.carId());
        cart.setModelId(request.modelId());
        cart.setSelectedPartIds(new HashMap<>(normalizeParts(request.selectedPartIds())));
        cart.setQuotedPrice(request.quotedPrice());
        cart.setQuoteExpiresAt(request.quoteExpiresAt());
    }

    private void validateRequest(SaveCartRequest request) {
        if (request.itemType() == null) {
            throw new DomainValidationException("Cart item type is required");
        }
        if (request.quotedPrice() == null || request.quotedPrice().signum() < 0) {
            throw new DomainValidationException("Quoted price must be zero or positive");
        }
        if (request.quoteExpiresAt() == null || !request.quoteExpiresAt().isAfter(Instant.now())) {
            throw new DomainValidationException("Quote expiry must be in the future");
        }
        if (request.itemType() == CartItemType.STOCK_CAR && (request.carId() == null || request.modelId() != null)) {
            throw new DomainValidationException("Stock car cart requires carId and must not include modelId");
        }
        if (request.itemType() == CartItemType.CONFIGURED_CAR
                && (request.modelId() == null || request.carId() != null)) {
            throw new DomainValidationException("Configured car cart requires modelId and must not include carId");
        }
    }

    private Cart ownedCart(UUID cartId, UUID customerId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        if (!cart.getCustomerId().equals(customerId)) {
            throw new EntityNotFoundException("Cart not found: " + cartId);
        }
        return cart;
    }

    private void expireIfNeeded(Cart cart) {
        if (cart.getStatus() == CartStatus.ACTIVE && !cart.getQuoteExpiresAt().isAfter(Instant.now())) {
            cart.setStatus(CartStatus.EXPIRED);
            cartRepository.save(cart);
            cartExpiryIndex.remove(cart.getId());
            throw new DomainValidationException("Cart quote has expired");
        }
    }

    private CartResponse toTrackedResponse(Cart cart) {
        return CartMapper.INSTANCE.toResponse(tracked(cart));
    }

    private Cart tracked(Cart cart) {
        cartExpiryIndex.track(cart.getId(), cart.getQuoteExpiresAt());
        return cart;
    }

    private Map<String, UUID> normalizeParts(Map<String, UUID> selectedPartIds) {
        return selectedPartIds == null ? Map.of() : Map.copyOf(selectedPartIds);
    }

    private void validateStatus(Cart cart, CartStatus required, String message) {
        if (cart.getStatus() != required) {
            throw new DomainValidationException(message + ", current status is " + cart.getStatus());
        }
    }
}
