package org.example.orderservice.application.port.out;

public interface AuthRateLimiter {

    String LOGIN = "LOGIN";
    String REGISTER = "REGISTER";
    String PASSWORD_RESET = "PASSWORD_RESET";

    void ensureAllowed(String action, String clientKey);

    void recordFailure(String action, String clientKey);

    void clear(String action, String clientKey);
}
