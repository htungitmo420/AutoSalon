package org.example.notificationservice.infrastructure.inbox;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.port.InboxEventProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedEventService implements InboxEventProcessor {
    private final ProcessedEventRepository repository;

    @Override
    @Transactional
    public boolean processOnce(UUID eventId, String topic, String messageKey, String traceId, Runnable handler) {
        if (repository.existsById(eventId)) {
            return false;
        }
        repository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .topic(topic)
                .messageKey(messageKey)
                .traceId(traceId)
                .processedAt(Instant.now())
                .build());
        handler.run();
        return true;
    }
}
