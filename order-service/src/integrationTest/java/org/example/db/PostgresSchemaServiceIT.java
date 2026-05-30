package org.example.db;

import jakarta.persistence.EntityManagerFactory;
import org.example.orderservice.OrderServiceApplication;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.application.repository.TestDriveRepository;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.domain.testdrive.model.TestDrive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = OrderServiceApplication.class)
@ActiveProfiles("integrationtest")
@Testcontainers
class PostgresSchemaServiceIT {

    private static final String SCHEMA = "auto_salon";
    private static final UUID SEEDED_COMMON_ORDER_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID SEEDED_CUSTOM_ORDER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID SEEDED_TEST_DRIVE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private static final String[] ORDER_TABLES = {
            "common_car_orders",
            "custom_car_orders",
            "test_drives",
            "custom_order_selected_parts",
            "auth_users",
            "auth_user_roles",
            "auth_refresh_tokens",
            "auth_password_reset_tokens",
            "auth_rate_limits",
            "outbox_events",
            "processed_events"
    };

    private static final String[] STORAGE_TABLES = {
            "car_models",
            "cars",
            "parts",
            "car_model_base_parts",
            "part_compatible_models"
    };

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_service")
            .withUsername("postgres")
            .withPassword(UUID.randomUUID().toString());

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CommonOrderRepository commonOrderRepository;

    @Autowired
    CustomOrderRepository customOrderRepository;

    @Autowired
    TestDriveRepository testDriveRepository;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void appStartsAndJpaMappingsAreInitialized() {
        assertNotNull(entityManagerFactory);
        assertFalse(entityManagerFactory.getMetamodel().getEntities().isEmpty());
    }

    @Test
    void liquibaseAppliesExpectedChangeSets() {
        assertAll(
                () -> assertEquals(8, countRows("SELECT COUNT(*) FROM databasechangelog")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '01-create-tables'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '02-constraints-indexes'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '03-seed-data'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '04-outbox-events'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '05-processed-events'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '06-reservation-reference'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '07-auth-users'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '08-production-identity'"))
        );
    }

    @Test
    void schemaContainsOrderServiceTables() {
        for (String table : ORDER_TABLES) {
            assertEquals(1,
                    countRows("""
                            SELECT COUNT(*) FROM information_schema.tables
                            WHERE table_schema = ? AND table_name = ?""", SCHEMA, table),
                    "Missing table: " + SCHEMA + "." + table);
        }

        for (String table : STORAGE_TABLES) {
            assertEquals(0,
                    countRows("""
                            SELECT COUNT(*) FROM information_schema.tables
                            WHERE table_schema = ? AND table_name = ?""", SCHEMA, table),
                    "Storage table must not be created by order-service: " + table);
        }
    }

    @Test
    void constraintsAndIndexesAreCreatedWithoutCrossServiceForeignKeys() {
        assertAll(
                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'custom_order_selected_parts'
                           AND constraint_type = 'PRIMARY KEY'
                           AND constraint_name = 'pk_custom_order_selected_parts'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'custom_order_selected_parts'
                           AND constraint_type = 'FOREIGN KEY'
                           AND constraint_name = 'fk_selected_parts_order_id'""", SCHEMA)),

                () -> assertEquals(0, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name IN ('common_car_orders', 'custom_car_orders', 'test_drives')
                           AND constraint_type = 'FOREIGN KEY'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'common_car_orders'
                           AND indexname = 'idx_common_orders_car_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'custom_car_orders'
                           AND indexname = 'idx_custom_orders_model_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'test_drives'
                           AND indexname = 'idx_test_drives_car_id_start'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'auth_users'
                           AND indexname = 'uq_auth_users_email_active'""", SCHEMA))
        );
    }

    @Test
    void seedDataIsLoadedForOrderServiceOnly() {
        assertAll(
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.common_car_orders WHERE id = ?", SEEDED_COMMON_ORDER_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.custom_car_orders WHERE id = ?", SEEDED_CUSTOM_ORDER_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.custom_order_selected_parts WHERE custom_order_id = ?",
                        SEEDED_CUSTOM_ORDER_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.test_drives WHERE id = ?", SEEDED_TEST_DRIVE_ID))
        );
    }

    @Test
    void repositoriesPersistOrdersWithExternalIds() {
        UUID customerId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        LocalDateTime startDateTime = LocalDateTime.of(2026, 9, 1, 10, 0);

        CommonCarOrder commonOrder = commonOrderRepository.save(CommonCarOrder.builder()
                .carId(carId)
                .customerId(customerId)
                .status(CommonOrderStatus.CREATED)
                .build());

        CustomCarOrder customOrder = customOrderRepository.save(CustomCarOrder.builder()
                .modelId(modelId)
                .customerId(customerId)
                .selectedPartIds(Map.of("WHEELS", partId))
                .status(CustomOrderStatus.CREATED)
                .build());

        TestDrive testDrive = testDriveRepository.save(TestDrive.builder()
                .carId(carId)
                .customerId(customerId)
                .status(TestDriveStatus.PENDING)
                .startDateTime(startDateTime)
                .build());

        assertAll(
                () -> assertNotNull(commonOrder.getId()),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.common_car_orders WHERE id = ? AND car_id = ?",
                        commonOrder.getId(), carId)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.custom_car_orders WHERE id = ? AND model_id = ?",
                        customOrder.getId(), modelId)),
                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM auto_salon.custom_order_selected_parts
                        WHERE custom_order_id = ? AND part_type = 'WHEELS' AND part_id = ?""",
                        customOrder.getId(), partId)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.test_drives WHERE id = ? AND car_id = ?",
                        testDrive.getId(), carId))
        );
    }

    private int countRows(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        assertNotNull(value);
        return value;
    }
}
