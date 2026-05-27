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
import org.example.commoncontracts.event.OrderRejectedEvent;
import org.example.commoncontracts.event.OrderSentForApprovalEvent;
import org.example.commoncontracts.event.ReservationExpiredEvent;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.example.commoncontracts.order.OrderType;
import org.example.storageservice.StorageServiceApplication;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.application.service.CarModelService;
import org.example.storageservice.application.service.AssemblyOrderService;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.application.service.PartService;
import org.example.storageservice.application.service.PartStockService;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.infrastructure.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(
        classes = StorageServiceApplication.class,
        properties = "spring.kafka.listener.auto-startup=true")
@ActiveProfiles("integrationtest")
@Testcontainers
class StorageKafkaE2EIT {

    private static final UUID MODEL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WHEELS_PART_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_service")
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
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    OutboxPublisher outboxPublisher;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    InventoryReservationService inventoryReservationService;

    @Autowired
    AssemblyOrderService assemblyOrderService;

    @Autowired
    InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    CarModelService carModelService;

    @Autowired
    PartService partService;

    @Autowired
    PartStockService partStockService;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void orderSentForApprovalIsConsumedAndApprovedEventIsPublished() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String traceId = UUID.randomUUID().toString();

        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                eventId,
                orderId,
                OrderType.CUSTOM,
                null,
                MODEL_ID,
                Map.of("WHEELS", WHEELS_PART_ID),
                traceId,
                Instant.now()
        );

        try (Consumer<String, Object> consumer = resultConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.ORDER_APPROVED, KafkaTopics.ORDER_REJECTED));

            kafkaTemplate.send(KafkaTopics.ORDER_SENT_FOR_APPROVAL, orderId.toString(), event)
                    .get(10, TimeUnit.SECONDS);

            OrderApprovedEvent approved = waitForApprovedEvent(consumer, orderId);

            assertEquals(orderId, approved.orderId());
            assertEquals(OrderType.CUSTOM, approved.orderType());
            assertEquals(traceId, approved.traceId());

            await("assembly order to be assembled", () -> countRows("""
                    SELECT COUNT(*) FROM auto_salon.assembly_orders
                    WHERE source_order_id = ? AND status = 'ASSEMBLED'""", orderId) == 1);
            await("outbox event to be published", () -> countRows("""
                    SELECT COUNT(*) FROM auto_salon.outbox_events
                    WHERE topic = ? AND message_key = ? AND status = 'PUBLISHED'""",
                    KafkaTopics.ORDER_APPROVED, orderId.toString()) == 1);

            assertEquals(1, countRows("""
                    SELECT COUNT(*) FROM auto_salon.processed_events
                    WHERE event_id = ?""", eventId));
            assertEquals(1, countRows("""
                    SELECT COUNT(*) FROM auto_salon.part_stocks
                    WHERE part_id = ? AND reserved_quantity = 1""", WHEELS_PART_ID));
        }
    }

    @Test
    void orderCancelledV1IsConsumedAndReleasesHeldConfiguration() throws Exception {
        ConfiguredInventory inventory = createConfiguredInventory("cancel-v1", 1);
        UUID orderId = UUID.randomUUID();
        var reservation = inventoryReservationService.reserveConfiguration(
                orderId, inventory.modelId(), Map.of("WHEELS", inventory.partId()),
                Instant.now().plusSeconds(900), "cancel-held");

        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(), 1, orderId, OrderType.CUSTOM, reservation.getId(),
                "Customer cancelled", "cancel-v1-trace", Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED_V1, orderId.toString(), event)
                .get(10, TimeUnit.SECONDS);

        await("cancel event to release held reservation", () -> "RELEASED".equals(reservationStatus(reservation.getId())));
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
        assertEquals(1, countRows("""
                SELECT COUNT(*) FROM auto_salon.processed_events
                WHERE event_id = ?""", event.eventId()));
    }

    @Test
    void expiredReservationPublishesReservationExpiredV1ThroughOutbox() throws Exception {
        ConfiguredInventory inventory = createConfiguredInventory("expire-v1", 1);
        UUID orderId = UUID.randomUUID();
        var reservation = inventoryReservationService.reserveConfiguration(
                orderId, inventory.modelId(), Map.of("WHEELS", inventory.partId()),
                Instant.now().plusSeconds(900), "expire-held");
        reservation.setExpiresAt(Instant.now().minusSeconds(1));
        inventoryReservationRepository.save(reservation);

        try (Consumer<String, Object> consumer = resultConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.RESERVATION_EXPIRED_V1));

            inventoryReservationService.expireHeldReservations();
            outboxPublisher.publishPendingEvents();

            ReservationExpiredEvent event = waitForEvent(consumer, orderId, ReservationExpiredEvent.class);

            assertEquals(reservation.getId(), event.reservationId());
            assertEquals(OrderType.CUSTOM, event.orderType());
            assertEquals("EXPIRED", reservationStatus(reservation.getId()));
            assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
        }
    }

    @Test
    void confirmedConfigurationPublishesAssemblyCompletedV1ThroughOutbox() throws Exception {
        ConfiguredInventory inventory = createConfiguredInventory("complete-v1", 1);
        UUID orderId = UUID.randomUUID();
        var reservation = inventoryReservationService.reserveConfiguration(
                orderId, inventory.modelId(), Map.of("WHEELS", inventory.partId()),
                Instant.now().plusSeconds(900), "complete-held");

        try (Consumer<String, Object> consumer = resultConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.ASSEMBLY_COMPLETED_V1));

            inventoryReservationService.confirmReservation(orderId, reservation.getId(), "complete-v1-trace");
            UUID assemblyOrderId = assemblyOrderIdFor(orderId);
            inventoryReservationService.assemble(assemblyOrderId, "complete-v1-trace");
            outboxPublisher.publishPendingEvents();

            AssemblyCompletedEvent event = waitForEvent(consumer, orderId, AssemblyCompletedEvent.class);

            assertEquals(reservation.getId(), event.reservationId());
            assertEquals("FULFILLED", reservationStatus(reservation.getId()));
            assertEquals(0, partStockService.getPartStock(inventory.stockId()).quantity());
        }
    }

    @Test
    void failedFulfillmentPublishesAssemblyFailedWithoutConsumingPartStock() throws Exception {
        ConfiguredInventory inventory = createConfiguredInventory("failure-v1", 1);
        UUID orderId = UUID.randomUUID();
        var reservation = inventoryReservationService.reserveConfiguration(
                orderId, inventory.modelId(), Map.of("WHEELS", inventory.partId()),
                Instant.now().plusSeconds(900), "failure-held");
        jdbcTemplate.update("""
                UPDATE auto_salon.part_stocks
                SET reserved_quantity = 0
                WHERE id = ?""", inventory.stockId());

        try (Consumer<String, Object> consumer = resultConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.ASSEMBLY_FAILED_V1));

            inventoryReservationService.confirmReservation(orderId, reservation.getId(), "failure-v1-trace");
            UUID assemblyOrderId = assemblyOrderIdFor(orderId);
            inventoryReservationService.assemble(assemblyOrderId, "failure-v1-trace");
            outboxPublisher.publishPendingEvents();

            AssemblyFailedEvent event = waitForEvent(consumer, orderId, AssemblyFailedEvent.class);

            assertEquals(reservation.getId(), event.reservationId());
            assertEquals("CONFIRMED", reservationStatus(reservation.getId()));
            assertEquals(1, partStockService.getPartStock(inventory.stockId()).quantity());
        }
    }

    private Consumer<String, Object> resultConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "storage-kafka-e2e-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "org.example.commoncontracts.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaConsumerFactory<String, Object>(props).createConsumer();
    }

    private OrderApprovedEvent waitForApprovedEvent(Consumer<String, Object> consumer, UUID orderId) {
        String expectedKey = orderId.toString();

        for (int attempt = 0; attempt < 60; attempt++) {
            outboxPublisher.publishPendingEvents();

            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));

            for (ConsumerRecord<String, Object> record : records) {
                if (!expectedKey.equals(record.key())) {
                    continue;
                }

                Object event = record.value();
                assertNotNull(event);

                if (event instanceof OrderApprovedEvent approvedEvent) {
                    return approvedEvent;
                }

                if (event instanceof OrderRejectedEvent rejectedEvent) {
                    return fail("Storage rejected orderId=" + orderId + ", reason=" + rejectedEvent.reason());
                }

                return fail("Expected OrderApprovedEvent but got " + event.getClass().getSimpleName());
            }
        }

        return fail("Timed out waiting for OrderApprovedEvent for orderId=" + orderId);
    }

    private <T> T waitForEvent(Consumer<String, Object> consumer, UUID orderId, Class<T> eventType) {
        String expectedKey = orderId.toString();

        for (int attempt = 0; attempt < 60; attempt++) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));

            for (ConsumerRecord<String, Object> record : records) {
                if (expectedKey.equals(record.key())) {
                    return assertInstanceOf(eventType, record.value());
                }
            }
        }

        return fail("Timed out waiting for " + eventType.getSimpleName() + " for orderId=" + orderId);
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

    private String reservationStatus(UUID reservationId) {
        return jdbcTemplate.queryForObject("""
                SELECT status FROM auto_salon.inventory_reservations
                WHERE id = ?""", String.class, reservationId);
    }

    private UUID assemblyOrderIdFor(UUID orderId) {
        return assemblyOrderService.listAssemblyOrders().stream()
                .filter(order -> order.sourceOrderId().equals(orderId))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private ConfiguredInventory createConfiguredInventory(String name, int quantity) {
        CarModelResponse model = carModelService.createCarModel(new SaveCarModelRequest(
                name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 280, 2.5,
                GearBoxType.AUTOMATIC, DrivetrainType.AWD, BigDecimal.valueOf(3100000), Map.of()));
        PartResponse part = partService.createPart(new SavePartRequest(
                name + "-wheels", PartType.WHEELS, BigDecimal.valueOf(100000), Set.of(model.id())));
        model = carModelService.updateCarModel(model.id(), new SaveCarModelRequest(
                name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 280, 2.5,
                GearBoxType.AUTOMATIC, DrivetrainType.AWD, BigDecimal.valueOf(3100000),
                Map.of(PartType.WHEELS, part.id())));
        PartStockResponse stock = partStockService.createPartStock(new SavePartStockRequest(part.id(), quantity, 0));
        return new ConfiguredInventory(model.id(), part.id(), stock.id());
    }

    private record ConfiguredInventory(UUID modelId, UUID partId, UUID stockId) {
    }
}
