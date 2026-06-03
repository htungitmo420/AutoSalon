package org.example.notificationservice.application.port;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
