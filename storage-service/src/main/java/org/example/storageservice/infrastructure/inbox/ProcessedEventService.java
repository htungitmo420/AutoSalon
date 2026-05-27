package org.example.storageservice.infrastructure.inbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean processOnce(UUID eventId, String topic, String messageKey, String traceId, Runnable handler) {
        if (processedEventRepository.existsById(eventId)) {
            return false;
        }

        processedEventRepository.save(ProcessedEvent.builder()
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
