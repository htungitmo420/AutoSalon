package org.example.orderservice.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    // Each time the publisher runs, it only takes a maximum of 20 events to send
    private static final int BATCH_SIZE = 20;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findReadyToPublish(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                Instant.now(), PageRequest.of(0, BATCH_SIZE));

        events.forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            Object payload = objectMapper.readValue(
                    event.getPayload(),
                    Class.forName(event.getPayloadType()));

            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload)
                    .get(10, TimeUnit.SECONDS);

            event.markPublished();
        } catch (Exception e) {
            event.markFailed(e.getMessage());
        }
    }
}
