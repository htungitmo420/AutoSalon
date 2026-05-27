package org.example.storageservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.example.commoncontracts.event.OrderApprovedEvent;
import org.example.commoncontracts.event.OrderRejectedEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.storageservice.infrastructure.outbox.OutboxService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageResultPublisher {

    private final OutboxService outboxService;

    public void publishApproved(OrderApprovedEvent event) {
        outboxService.save(KafkaTopics.ORDER_APPROVED, event.orderId().toString(), event);
    }

    public void publishRejected(OrderRejectedEvent event) {
        outboxService.save(KafkaTopics.ORDER_REJECTED, event.orderId().toString(), event);
    }
}
