package org.example.db;

import jakarta.persistence.EntityManagerFactory;
import org.example.storageservice.StorageServiceApplication;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.service.CarModelService;
import org.example.storageservice.application.service.CarService;
import org.example.storageservice.application.service.CatalogService;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = StorageServiceApplication.class)
@ActiveProfiles("integrationtest")
@Testcontainers
class PostgresSchemaServiceIT {

    private static final String SCHEMA = "auto_salon";
    private static final UUID SEEDED_MODEL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SEEDED_CAR_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ENGINE_PART_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID WHEELS_PART_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String[] STORAGE_TABLES = {
            "car_models",
            "cars",
            "parts",
            "car_model_base_parts",
            "part_compatible_models",
            "part_stocks",
            "assembly_orders",
            "assembly_order_required_parts",
            "inventory_reservations",
            "reservation_required_parts",
            "outbox_events",
            "processed_events"
    };

    private static final String[] ORDER_TABLES = {
            "common_car_orders",
            "custom_car_orders",
            "test_drives",
            "custom_order_selected_parts"
    };

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_service")
            .withUsername("postgres")
            .withPassword("123456");

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
    CarModelService carModelService;

    @Autowired
    CarService carService;

    @Autowired
    CatalogService catalogService;

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
                () -> assertEquals(6, countRows("SELECT COUNT(*) FROM databasechangelog")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '01-create-storage-tables'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '02-storage-constraints-indexes'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '03-storage-seed-data'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '04-outbox-events'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '05-processed-events'")),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM databasechangelog WHERE id = '06-inventory-reservations'"))
        );
    }

    @Test
    void schemaContainsOnlyStorageServiceTables() {
        for (String table : STORAGE_TABLES) {
            assertEquals(1,
                    countRows("""
                            SELECT COUNT(*) FROM information_schema.tables
                            WHERE table_schema = ? AND table_name = ?""", SCHEMA, table),
                    "Missing table: " + SCHEMA + "." + table);
        }

        for (String table : ORDER_TABLES) {
            assertEquals(0,
                    countRows("""
                            SELECT COUNT(*) FROM information_schema.tables
                            WHERE table_schema = ? AND table_name = ?""", SCHEMA, table),
                    "Order table must not be created by storage-service: " + table);
        }
    }

    @Test
    void constraintsAndIndexesAreCreated() {
        assertAll(
                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'cars'
                           AND constraint_type = 'FOREIGN KEY'
                           AND constraint_name = 'fk_cars_model_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'car_model_base_parts'
                           AND constraint_type = 'PRIMARY KEY'
                           AND constraint_name = 'pk_car_model_base_parts'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'part_compatible_models'
                           AND constraint_type = 'PRIMARY KEY'
                           AND constraint_name = 'pk_part_compatible_models'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'cars'
                           AND indexname = 'idx_cars_model_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'part_stocks'
                           AND constraint_type = 'UNIQUE'
                           AND constraint_name = 'uq_part_stocks_part_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'assembly_order_required_parts'
                           AND constraint_type = 'PRIMARY KEY'
                           AND constraint_name = 'pk_assembly_order_required_parts'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM information_schema.table_constraints
                        WHERE table_schema = ?
                           AND table_name = 'assembly_orders'
                           AND constraint_type = 'UNIQUE'
                           AND constraint_name = 'uq_assembly_orders_source_order_id'""", SCHEMA)),

                () -> assertEquals(1, countRows("""
                        SELECT COUNT(*) FROM pg_indexes
                        WHERE schemaname = ?
                           AND tablename = 'assembly_orders'
                           AND indexname = 'idx_assembly_orders_source_order_id'""", SCHEMA))
        );
    }

    @Test
    void seedDataIsLoaded() {
        assertAll(
                () -> assertTrue(countRows("SELECT COUNT(*) FROM auto_salon.car_models") > 0),
                () -> assertTrue(countRows("SELECT COUNT(*) FROM auto_salon.parts") > 0),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.car_models WHERE id = ?", SEEDED_MODEL_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.parts WHERE id = ?", ENGINE_PART_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.part_stocks WHERE part_id = ?", ENGINE_PART_ID)),
                () -> assertEquals(1, countRows(
                        "SELECT COUNT(*) FROM auto_salon.cars WHERE id = ?", SEEDED_CAR_ID))
        );
    }

    @Test
    void serviceCreateCarPersistsIntoDatabase() {
        int before = countRows("SELECT COUNT(*) FROM auto_salon.cars");

        CarModelResponse modelResponse = carModelService.createCarModel(new SaveCarModelRequest(
                "IT-Model", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                200, 2.0, GearBoxType.AUTOMATIC,
                DrivetrainType.RWD, BigDecimal.valueOf(4500000), Map.of()));

        CarResponse carResponse = carService.createCar(new SaveCarRequest(
                modelResponse.id(), Color.BLACK, BigDecimal.valueOf(500000)));

        int persisted = countRows("SELECT COUNT(*) FROM auto_salon.cars WHERE id = ?", carResponse.id());
        int after = countRows("SELECT COUNT(*) FROM auto_salon.cars");

        List<UUID> storedModelIds = jdbcTemplate.query(
                "SELECT model_id FROM auto_salon.cars WHERE id = ?",
                (rs, rowNum) -> rs.getObject("model_id", UUID.class),
                carResponse.id());

        assertAll(
                () -> assertEquals(1, persisted),
                () -> assertEquals(before + 1, after),
                () -> assertEquals(1, storedModelIds.size()),
                () -> assertEquals(modelResponse.id(), storedModelIds.getFirst())
        );
    }

    @Test
    void createCarWithUnknownModel() {
        int before = countRows("SELECT COUNT(*) FROM auto_salon.cars");

        assertThrows(EntityNotFoundException.class, () -> carService.createCar(new SaveCarRequest(
                UUID.randomUUID(), Color.BLACK, BigDecimal.valueOf(500000))));

        int after = countRows("SELECT COUNT(*) FROM auto_salon.cars");
        assertEquals(before, after);
    }

    @Test
    void specificationFilterByBrandAndComponents() {
        CarFilterRequest filter = new CarFilterRequest(null, null,
                Brand.BMW, null, null, null, null,
                null, null, null, null,
                null, null, Set.of(ENGINE_PART_ID, WHEELS_PART_ID));

        List<UUID> resultIds = catalogService.listCars(filter).stream()
                .map(CarResponse::id)
                .toList();

        assertEquals(1, resultIds.size());
        assertEquals(SEEDED_CAR_ID, resultIds.getFirst());
    }

    @Test
    void specificationFilterExecutesAsSingleSqlQuery() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        catalogService.listCars(new CarFilterRequest(
                null, null, Brand.BMW, null, null,
                null, null, null, null,
                null, null, null, null,
                Set.of(ENGINE_PART_ID)));

        assertEquals(1, statistics.getPrepareStatementCount());
    }

    private int countRows(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        assertNotNull(value);
        return value;
    }
}
