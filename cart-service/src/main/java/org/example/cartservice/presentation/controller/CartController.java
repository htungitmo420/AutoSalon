package org.example.cartservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.application.dto.request.SaveCartRequest;
import org.example.cartservice.application.dto.response.CartResponse;
import org.example.cartservice.application.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public CartResponse save(@RequestBody SaveCartRequest request) {
        return cartService.saveCurrentCart(request);
    }

    @GetMapping
    public CartResponse getCurrent() {
        return cartService.getCurrentCart();
    }

    @GetMapping("/{cartId}")
    public CartResponse get(@PathVariable UUID cartId) {
        return cartService.getCart(cartId);
    }

    @DeleteMapping("/{cartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable UUID cartId) {
        cartService.clearCart(cartId);
    }
}
