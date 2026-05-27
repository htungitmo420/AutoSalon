package org.example.storageservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncontracts.event.OrderApprovedEvent;
import org.example.commoncontracts.event.OrderRejectedEvent;
import org.example.commoncontracts.event.OrderSentForApprovalEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.service.AssemblyOrderService;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.infrastructure.inbox.ProcessedEventService;
import org.example.storageservice.infrastructure.logging.TraceContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSentForApprovalListener {

    private final ProcessedEventService processedEventService;
    private final AssemblyOrderService assemblyOrderService;
    private final StorageResultPublisher storageResultPublisher;

    @KafkaListener(topics = KafkaTopics.ORDER_SENT_FOR_APPROVAL)
    public void handle(OrderSentForApprovalEvent event) {
        TraceContext.runWithTraceId(event.traceId(), () -> {
            log.info("Received order sent for approval event, eventId={}, orderId={}, orderType={}",
                    event.eventId(), event.orderId(), event.orderType());

            boolean processed = processedEventService.processOnce(
                    event.eventId(),
                    KafkaTopics.ORDER_SENT_FOR_APPROVAL,
                    event.orderId().toString(),
                    event.traceId(),
                    () -> processOrderSentForApproval(event));

            if (!processed) {
                log.info("Skipped duplicate order sent for approval event, eventId={}", event.eventId());
            }
        });
    }

    private void processOrderSentForApproval(OrderSentForApprovalEvent event) {
        AssemblyOrderResponse created = null;
        try {
            created = assemblyOrderService.createAssemblyOrder(
                    new SaveAssemblyOrderRequest(
                            event.orderId(),
                            SourceOrderType.valueOf(event.orderType().name()),
                            event.carId(),
                            event.modelId(),
                            event.selectedPartIds(),
                            null,
                            null,
                            null
                    )
            );

            assemblyOrderService.assemble(created.id());

            storageResultPublisher.publishApproved(new OrderApprovedEvent(
                    UUID.randomUUID(),
                    event.orderId(),
                    event.orderType(),
                    event.traceId(),
                    Instant.now()
            ));

            log.info("Assembly order approved, sourceOrderId={}", event.orderId());
        } catch (Exception ex) {
            if (created != null) {
                assemblyOrderService.failToAssemble(created.id(), ex.getMessage());
            }
            storageResultPublisher.publishRejected(new OrderRejectedEvent(
                    UUID.randomUUID(),
                    event.orderId(),
                    event.orderType(),
                    ex.getMessage(),
                    event.traceId(),
                    Instant.now()
            ));

            log.warn("Assembly order rejected, sourceOrderId={}, reason={}", event.orderId(), ex.getMessage());
        }
    }
}
