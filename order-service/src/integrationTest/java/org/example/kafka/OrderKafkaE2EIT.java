package org.example.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.commoncontracts.event.AssemblyCompletedEvent;
import org.example.commoncontracts.event.AssemblyFailedEvent;
import org.example.commoncontracts.event.OrderApprovedEvent;
import org.example.commoncontracts.event.OrderCancelledEvent;
import org.example.commoncontracts.event.ReservationExpiredEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.orderservice.OrderServiceApplication;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.example.orderservice.infrastructure.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(
        classes = OrderServiceApplication.class,
        properties = "spring.kafka.listener.auto-startup=true")
@ActiveProfiles("integrationtest")
@Testcontainers
class OrderKafkaE2EIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_service")
            .withUsername("postgres")
            .withPassword("123456");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withKraft();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    OrderService orderService;

    @Autowired
    CommonOrderRepository commonOrderRepository;

    @Autowired
    CustomOrderRepository customOrderRepository;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    OutboxPublisher outboxPublisher;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void cancelHeldOrderPublishesOrderCancelledThroughOutbox() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        CommonCarOrder order = commonOrderRepository.save(CommonCarOrder.builder()
                .carId(carId)
                .customerId(UUID.randomUUID())
                .reservationId(reservationId)
                .status(CommonOrderStatus.WAITING_FOR_PAYMENT)
                .build());

        try (Consumer<String, Object> consumer = eventConsumer("order-service-producer")) {
            consumer.subscribe(List.of(KafkaTopics.ORDER_CANCELLED_V1));

            runAsManager(() -> orderService.cancelCommonOrder(order.getId()));
            outboxPublisher.publishPendingEvents();

            OrderCancelledEvent event = waitForEvent(
                    consumer, order.getId(), OrderCancelledEvent.class);

            assertEquals(order.getId(), event.orderId());
            assertEquals(OrderType.COMMON, event.orderType());
            assertEquals(reservationId, event.reservationId());
            assertNotNull(event.traceId());

            await("order cancelled outbox event to be published", () -> countRows("""
                    SELECT COUNT(*) FROM auto_salon.outbox_events
                    WHERE topic = ? AND message_key = ? AND status = 'PUBLISHED'""",
                    KafkaTopics.ORDER_CANCELLED_V1, order.getId().toString()) == 1);
        }
    }

    @Test
    void approvedEventIsConsumedAndMarksPaidCommonOrderReadyForPickup() throws Exception {
        CommonCarOrder order = commonOrderRepository.save(CommonCarOrder.builder()
                .carId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(CommonOrderStatus.PAID)
                .build());
        UUID eventId = UUID.randomUUID();

        OrderApprovedEvent event = new OrderApprovedEvent(
                eventId,
                order.getId(),
                OrderType.COMMON,
                UUID.randomUUID().toString(),
                Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_APPROVED, order.getId().toString(), event)
                .get(10, TimeUnit.SECONDS);

        await("common order to become ready for pickup", () -> CommonOrderStatus.READY_FOR_PICKUP.name().equals(
                jdbcTemplate.queryForObject("""
                        SELECT status FROM auto_salon.common_car_orders
                        WHERE id = ?""", String.class, order.getId())));

        assertEquals(1, countRows("""
                SELECT COUNT(*) FROM auto_salon.processed_events
                WHERE event_id = ?""", eventId));
    }

    @Test
    void reservationExpiredEventCancelsAnOrderWaitingForPayment() throws Exception {
        CommonCarOrder order = commonOrderRepository.save(CommonCarOrder.builder()
                .carId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .reservationId(UUID.randomUUID())
                .status(CommonOrderStatus.WAITING_FOR_PAYMENT)
                .build());
        UUID eventId = UUID.randomUUID();

        ReservationExpiredEvent event = new ReservationExpiredEvent(
                eventId, 1, order.getId(), OrderType.COMMON, order.getReservationId(),
                "reservation-expired-trace", Instant.now());

        kafkaTemplate.send(KafkaTopics.RESERVATION_EXPIRED_V1, order.getId().toString(), event)
                .get(10, TimeUnit.SECONDS);

        await("expired reservation to cancel common order", () -> CommonOrderStatus.CANCELLED.name().equals(
                statusOf("common_car_orders", order.getId())));
        assertEquals(1, processedEventCount(eventId));
    }

    @Test
    void assemblyCompletedEventMarksAssemblingCustomOrderReadyForPickup() throws Exception {
        CustomCarOrder order = customOrderRepository.save(customOrder(CustomOrderStatus.ASSEMBLING));
        UUID eventId = UUID.randomUUID();

        AssemblyCompletedEvent event = new AssemblyCompletedEvent(
                eventId, 1, order.getId(), order.getReservationId(), UUID.randomUUID(),
                "assembly-completed-trace", Instant.now());

        kafkaTemplate.send(KafkaTopics.ASSEMBLY_COMPLETED_V1, order.getId().toString(), event)
                .get(10, TimeUnit.SECONDS);

        await("assembly completion to make custom order ready", () -> CustomOrderStatus.READY_FOR_PICKUP.name().equals(
                statusOf("custom_car_orders", order.getId())));
        assertEquals(1, processedEventCount(eventId));
    }

    @Test
    void assemblyFailedEventMarksAssemblingCustomOrderRefundRequired() throws Exception {
        CustomCarOrder order = customOrderRepository.save(customOrder(CustomOrderStatus.ASSEMBLING));
        UUID eventId = UUID.randomUUID();

        AssemblyFailedEvent event = new AssemblyFailedEvent(
                eventId, 1, order.getId(), order.getReservationId(), UUID.randomUUID(),
                "Parts damaged during assembly", "assembly-failed-trace", Instant.now());

        kafkaTemplate.send(KafkaTopics.ASSEMBLY_FAILED_V1, order.getId().toString(), event)
                .get(10, TimeUnit.SECONDS);

        await("assembly failure to require refund", () -> CustomOrderStatus.REFUND_REQUIRED.name().equals(
                statusOf("custom_car_orders", order.getId())));
        assertEquals(1, processedEventCount(eventId));
    }

    private Consumer<String, Object> eventConsumer(String groupPrefix) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupPrefix + "-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "org.example.commoncontracts.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaConsumerFactory<String, Object>(props).createConsumer();
    }

    private <T> T waitForEvent(Consumer<String, Object> consumer, UUID orderId, Class<T> eventType) {
        String expectedKey = orderId.toString();

        for (int attempt = 0; attempt < 60; attempt++) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));

            for (ConsumerRecord<String, Object> record : records) {
                if (!expectedKey.equals(record.key())) {
                    continue;
                }

                assertNotNull(record.value());
                return assertInstanceOf(eventType, record.value());
            }
        }

        return fail("Timed out waiting for " + eventType.getSimpleName() + " for orderId=" + orderId);
    }


    private void runAsManager(Runnable action) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new TestingAuthenticationToken("manager", "n/a", "ROLE_MANAGER"));
        SecurityContextHolder.setContext(context);
        try {
            action.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void await(String description, BooleanSupplier assertion) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (assertion.getAsBoolean()) {
                return;
            }
            Thread.sleep(200);
        }
        fail("Timed out waiting for " + description);
    }

    private int countRows(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        assertNotNull(value);
        return value;
    }

    private CustomCarOrder customOrder(CustomOrderStatus status) {
        return CustomCarOrder.builder()
                .modelId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .reservationId(UUID.randomUUID())
                .status(status)
                .build();
    }

    private String statusOf(String tableName, UUID orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM auto_salon." + tableName + " WHERE id = ?",
                String.class,
                orderId);
    }

    private int processedEventCount(UUID eventId) {
        return countRows("""
                SELECT COUNT(*) FROM auto_salon.processed_events
                WHERE event_id = ?""", eventId);
    }
}
