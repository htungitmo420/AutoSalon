package org.example.paymentservice.application.port;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
