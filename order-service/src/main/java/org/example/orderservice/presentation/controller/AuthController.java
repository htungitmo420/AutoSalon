package org.example.orderservice.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.request.LoginRequest;
import org.example.orderservice.application.dto.request.PasswordResetConfirmRequest;
import org.example.orderservice.application.dto.request.PasswordResetRequest;
import org.example.orderservice.application.dto.request.RefreshTokenRequest;
import org.example.orderservice.application.dto.request.RegisterRequest;
import org.example.orderservice.application.dto.response.AuthMessageResponse;
import org.example.orderservice.application.dto.response.AuthResponse;
import org.example.orderservice.application.dto.response.AuthenticatedUserResponse;
import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.example.orderservice.application.service.AuthService;
import org.example.orderservice.security.ClientAddressResolver;
import org.example.orderservice.security.RefreshTokenCookieService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;
    private final ClientAddressResolver clientAddressResolver;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.register(request, clientAddressResolver.resolve(httpRequest));
        return writeRefreshCookie(httpResponse, response);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request, clientAddressResolver.resolve(httpRequest));
        return writeRefreshCookie(httpResponse, response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.refresh(new RefreshTokenRequest(resolveRefreshToken(request, httpRequest)));
        return writeRefreshCookie(httpResponse, response);
    }

    @PostMapping("/logout")
    public AuthMessageResponse logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthMessageResponse response = authService.logout(new RefreshTokenRequest(resolveRefreshToken(request, httpRequest)));
        refreshTokenCookieService.clear(httpResponse);
        return response;
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public AuthMessageResponse logoutAll(HttpServletResponse httpResponse) {
        AuthMessageResponse response = authService.logoutAll(currentUserProvider.getCurrentUserId());
        refreshTokenCookieService.clear(httpResponse);
        return response;
    }

    @PostMapping("/password-reset/request")
    public AuthMessageResponse requestPasswordReset(
            @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        return authService.requestPasswordReset(request, clientAddressResolver.resolve(httpRequest));
    }

    @PostMapping("/password-reset/confirm")
    public AuthMessageResponse resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AuthenticatedUserResponse me() {
        return authService.currentUser(currentUserProvider.getCurrentUserId());
    }

    private AuthResponse writeRefreshCookie(HttpServletResponse httpResponse, AuthResponse response) {
        refreshTokenCookieService.write(httpResponse, response.refreshToken(), response.refreshTokenExpiresInSeconds());
        return response;
    }

    private String resolveRefreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        return refreshTokenCookieService.read(httpRequest);
    }
}
