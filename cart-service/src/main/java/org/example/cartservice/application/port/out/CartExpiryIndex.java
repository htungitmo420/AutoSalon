package org.example.cartservice.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface CartExpiryIndex {

    void track(UUID cartId, Instant expiresAt);

    void remove(UUID cartId);
}
