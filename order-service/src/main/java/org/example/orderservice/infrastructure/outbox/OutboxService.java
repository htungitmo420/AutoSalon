package org.example.orderservice.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OutboxService {

    private final OutboxEventRepository  outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void save(String topic, String messageKey, Object event) {
        try {
            outboxEventRepository.save(OutboxEvent.builder()
                            .topic(topic)
                            .messageKey(messageKey)
                            .payloadType(event.getClass().getName())
                            .payload(objectMapper.writeValueAsString(event))
                            .status(OutboxEventStatus.PENDING)
                            .attempts(0)
                            .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox event", e);
        }
    }
}