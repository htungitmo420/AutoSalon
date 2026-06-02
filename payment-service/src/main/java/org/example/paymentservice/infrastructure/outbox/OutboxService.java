package org.example.paymentservice.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public void save(String topic, String messageKey, Object event) {
        try {
            repository.save(OutboxEvent.builder()
                    .topic(topic)
                    .messageKey(messageKey)
                    .payloadType(event.getClass().getName())
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxEventStatus.PENDING)
                    .build());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize outbox event", exception);
        }
    }
}
