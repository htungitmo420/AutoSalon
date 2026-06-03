package org.example.notificationservice.application.port;

import java.util.UUID;

public interface InboxEventProcessor {
    boolean processOnce(UUID eventId, String topic, String messageKey, String traceId, Runnable handler);
}
