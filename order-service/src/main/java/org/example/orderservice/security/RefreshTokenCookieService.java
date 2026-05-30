package org.example.orderservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class RefreshTokenCookieService {

    @Value("${security.auth.refresh-cookie.name:AUTO_SALON_REFRESH}")
    private String cookieName;

    @Value("${security.auth.refresh-cookie.path:/api/auth}")
    private String cookiePath;

    @Value("${security.auth.refresh-cookie.domain:}")
    private String cookieDomain;

    @Value("${security.auth.refresh-cookie.secure:false}")
    private boolean secure;

    @Value("${security.auth.refresh-cookie.same-site:Strict}")
    private String sameSite;

    public void write(HttpServletResponse response, String refreshToken, long ttlSeconds) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(refreshToken, Duration.ofSeconds(ttlSeconds)).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(cookiePath)
                .maxAge(maxAge);
        if (!cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }
}
