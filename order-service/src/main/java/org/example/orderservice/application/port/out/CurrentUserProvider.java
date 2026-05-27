package org.example.orderservice.application.port.out;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();

    boolean hasElevatedReadAccess();
}
