package org.example.rest;

import org.example.storageservice.StorageServiceApplication;
import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.part.enums.PartType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = StorageServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Testcontainers
class RestApiIT {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-100000000001");
    private static final UUID WAREHOUSE_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-100000000002");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-100000000004");

    private static final UUID SEED_CAR_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SEED_ENGINE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

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
    WebTestClient webTestClient;

    @MockBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpJwtDecoder() {
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> jwtForToken(invocation.getArgument(0)));
    }

    @Nested
    @DisplayName("Car Models")
    class CarModelTests {

        @Test
        void crud() {
            CarModelResponse created = createModel("IT-Model");

            assertNotNull(created.id());
            assertEquals("IT-Model", created.modelName());

            CarModelResponse got = warehouseClient().get().uri("/api/models/{id}", created.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarModelResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(created.id(), got.id());

            List<CarModelResponse> list = warehouseClient().get().uri("/api/models")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(CarModelResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(list);
            assertTrue(list.stream().anyMatch(m -> m.id().equals(created.id())));

            CarModelResponse updated = warehouseClient().put().uri("/api/models/{id}", created.id())
                    .bodyValue(new SaveCarModelRequest("Updated", Brand.AUDI, BodyType.SUV, FuelType.DIESEL,
                            400, 4.0, GearBoxType.MANUAL, DrivetrainType.FWD,
                            BigDecimal.valueOf(7000000), Map.of()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarModelResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(updated);
            assertEquals("Updated", updated.modelName());

            warehouseClient().delete().uri("/api/models/{id}", created.id())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        void notFound() {
            warehouseClient().get().uri("/api/models/{id}", UUID.randomUUID())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("Parts")
    class PartTests {

        @Test
        void crud() {
            UUID modelId = createModel("Part-CRUD").id();
            PartResponse part = createPart("Brake kit", PartType.OTHER, Set.of(modelId));

            assertNotNull(part.id());
            assertEquals("Brake kit", part.name());

            PartResponse got = warehouseClient().get().uri("/api/parts/{id}", part.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PartResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(part.id(), got.id());

            PartResponse updated = warehouseClient().put().uri("/api/parts/{id}", part.id())
                    .bodyValue(new SavePartRequest("Race brake", PartType.OTHER,
                            BigDecimal.valueOf(120000), Set.of(modelId)))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PartResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(updated);
            assertEquals("Race brake", updated.name());

            warehouseClient().delete().uri("/api/parts/{id}", part.id())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        void notFound() {
            warehouseClient().get().uri("/api/parts/{id}", UUID.randomUUID())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("Cars")
    class CarTests {

        @Test
        void crud() {
            UUID modelId = createModel("Car-CRUD").id();
            CarResponse car = createCar(modelId);

            assertNotNull(car.id());
            assertEquals(modelId, car.modelId());

            CarResponse got = warehouseClient().get().uri("/api/cars/{id}", car.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(car.id(), got.id());

            CarResponse updated = warehouseClient().put().uri("/api/cars/{id}", car.id())
                    .bodyValue(new SaveCarRequest(modelId, Color.WHITE, BigDecimal.valueOf(3500000)))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(updated);
            assertEquals(Color.WHITE, updated.color());

            warehouseClient().delete().uri("/api/cars/{id}", car.id())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        void notFound() {
            warehouseClient().get().uri("/api/cars/{id}", UUID.randomUUID())
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void createWithUnknownModel() {
            warehouseClient().post().uri("/api/cars")
                    .bodyValue(new SaveCarRequest(UUID.randomUUID(), Color.BLACK, BigDecimal.ONE))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("Warehouse Workflow")
    class WarehouseWorkflowTests {

        @Test
        void partStockCrud() {
            UUID modelId = createModel("Stock-Model").id();
            PartResponse part = createPart("Stock wheels", PartType.WHEELS, Set.of(modelId));

            PartStockResponse stock = warehouseClient().post().uri("/api/part-stocks")
                    .bodyValue(new SavePartStockRequest(part.id(), 10, 1))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(PartStockResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(stock);
            assertEquals(9, stock.availableQuantity());

            PartStockResponse got = warehouseClient().get().uri("/api/part-stocks/{id}", stock.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PartStockResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(stock.id(), got.id());

            PartStockResponse updated = warehouseClient().put().uri("/api/part-stocks/{id}", stock.id())
                    .bodyValue(new SavePartStockRequest(part.id(), 12, 2))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PartStockResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(updated);
            assertEquals(10, updated.availableQuantity());

            warehouseClient().delete().uri("/api/part-stocks/{id}", stock.id())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        void assemblyOrderCrudAndAssemble() {
            SetupResult setup = setupModelWithPart(PartType.WHEELS);
            PartStockResponse stock = warehouseClient().post().uri("/api/part-stocks")
                    .bodyValue(new SavePartStockRequest(setup.part().id(), 3, 0))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(PartStockResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(stock);

            AssemblyOrderResponse created = warehouseClient().post().uri("/api/assembly-orders")
                    .bodyValue(new SaveAssemblyOrderRequest(
                            UUID.randomUUID(),
                            SourceOrderType.CUSTOM,
                            null,
                            setup.model().id(),
                            Map.of("WHEELS", setup.part().id()),
                            UUID.randomUUID(),
                            AssemblyOrderStatus.CREATED,
                            null))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AssemblyOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(created);
            assertEquals(AssemblyOrderStatus.CREATED, created.status());

            AssemblyOrderResponse got = warehouseClient().get().uri("/api/assembly-orders/{id}", created.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AssemblyOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(created.id(), got.id());

            AssemblyOrderResponse assembled = warehouseClient().patch()
                    .uri("/api/assembly-orders/{id}/assemble", created.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AssemblyOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(assembled);
            assertEquals(AssemblyOrderStatus.ASSEMBLED, assembled.status());

            warehouseClient().delete().uri("/api/assembly-orders/{id}", created.id())
                    .exchange()
                    .expectStatus().isNoContent();
        }
    }

    @Nested
    @DisplayName("Catalog")
    class CatalogTests {

        @Test
        void getCarFromCatalog() {
            CarResponse car = userClient().get().uri("/api/catalog/cars/{id}", SEED_CAR_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(car);
            assertEquals(SEED_CAR_ID, car.id());
        }

        @Test
        void filterByBrand() {
            List<CarResponse> result = postFilter(new CarFilterRequest(null, null, Brand.BMW,
                    null, null, null, null, null,
                    null, null, null, null, null, null));

            assertFalse(result.isEmpty());
            assertTrue(result.stream().anyMatch(c -> c.id().equals(SEED_CAR_ID)));
        }

        @Test
        void filterByComponent() {
            List<CarResponse> result = postFilter(new CarFilterRequest(null, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null,
                    Set.of(SEED_ENGINE_ID)));

            assertTrue(result.stream().anyMatch(c -> c.id().equals(SEED_CAR_ID)));
        }

        @Test
        void filterNoMatch() {
            List<CarResponse> result = postFilter(new CarFilterRequest(null, null, Brand.FORD,
                    null, null, null, null, null,
                    null, null, null, null, null, null));

            assertFalse(result.stream().anyMatch(c -> c.id().equals(SEED_CAR_ID)));
        }
    }

    @Nested
    @DisplayName("Configurator")
    class ConfiguratorTests {

        @Test
        void buildConfiguration() {
            SetupResult setup = setupModelWithPart(PartType.WHEELS);

            CarConfigurationResponse config = userClient().post()
                    .uri("/api/configurator/models/{modelId}", setup.model().id())
                    .bodyValue(new CarConfigurationRequest(Map.of(PartType.WHEELS, setup.part().id())))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CarConfigurationResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(config);
            assertNotNull(config.totalPrice());
            assertEquals(setup.model().id(), config.carModel().id());
        }

        @Test
        void unknownModel() {
            userClient().post().uri("/api/configurator/models/{modelId}", UUID.randomUUID())
                    .bodyValue(new CarConfigurationRequest(Map.of()))
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void incompatiblePart() {
            SetupResult setup = setupModelWithPart(PartType.WHEELS);
            UUID otherModelId = createModel("Other").id();
            PartResponse badPart = createPart("wrong", PartType.WHEELS, Set.of(otherModelId));

            userClient().post().uri("/api/configurator/models/{modelId}", setup.model().id())
                    .bodyValue(new CarConfigurationRequest(Map.of(PartType.WHEELS, badPart.id())))
                    .exchange()
                    .expectStatus().isEqualTo(409);
        }
    }

    private CarModelResponse createModel(String name) {
        return warehouseClient().post().uri("/api/models")
                .bodyValue(new SaveCarModelRequest(name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                        250, 2.5, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                        BigDecimal.valueOf(3000000), Map.of()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarModelResponse.class)
                .returnResult().getResponseBody();
    }

    private CarModelResponse createModelWithParts(String name, Map<PartType, UUID> basePartIds) {
        return warehouseClient().post().uri("/api/models")
                .bodyValue(new SaveCarModelRequest(name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                        250, 2.5, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                        BigDecimal.valueOf(3000000), basePartIds))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarModelResponse.class)
                .returnResult().getResponseBody();
    }

    private CarResponse createCar(UUID modelId) {
        return warehouseClient().post().uri("/api/cars")
                .bodyValue(new SaveCarRequest(modelId, Color.BLACK, BigDecimal.valueOf(2500000)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarResponse.class)
                .returnResult().getResponseBody();
    }

    private PartResponse createPart(String name, PartType type, Set<UUID> compatibleModelIds) {
        return warehouseClient().post().uri("/api/parts")
                .bodyValue(new SavePartRequest(name, type, BigDecimal.valueOf(50000), compatibleModelIds))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PartResponse.class)
                .returnResult().getResponseBody();
    }

    private SetupResult setupModelWithPart(PartType partType) {
        PartResponse part = createPart("part-" + partType, partType, Set.of());
        CarModelResponse model = createModelWithParts(
                "model-" + UUID.randomUUID().toString().substring(0, 6),
                Map.of(partType, part.id()));

        PartResponse updatedPart = warehouseClient().put().uri("/api/parts/{id}", part.id())
                .bodyValue(new SavePartRequest(part.name(), partType, part.surcharge(), Set.of(model.id())))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PartResponse.class)
                .returnResult().getResponseBody();

        return new SetupResult(model, updatedPart);
    }

    private List<CarResponse> postFilter(CarFilterRequest filter) {
        return userClient().post().uri("/api/catalog/cars/filter")
                .bodyValue(filter)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CarResponse.class)
                .returnResult().getResponseBody();
    }

    private WebTestClient userClient() {
        return authorizedClient(USER_ID, "USER");
    }

    private WebTestClient warehouseClient() {
        return authorizedClient(WAREHOUSE_ADMIN_ID, "WAREHOUSE_ADMIN");
    }

    private WebTestClient adminClient() {
        return authorizedClient(ADMIN_ID, "ADMIN");
    }

    private WebTestClient authorizedClient(UUID userId, String... roles) {
        String token = tokenValue(userId, roles);
        return webTestClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    private String tokenValue(UUID userId, String... roles) {
        return "sub." + userId + ".roles." + String.join("~", roles);
    }

    private Jwt jwtForToken(String token) {
        String marker = ".roles.";
        if (!token.startsWith("sub.") || !token.contains(marker)) {
            throw new IllegalArgumentException("Unexpected token format");
        }

        int markerIndex = token.indexOf(marker);
        String userIdPart = token.substring(4, markerIndex);
        String rolesPart = token.substring(markerIndex + marker.length());

        UUID userId = UUID.fromString(userIdPart);
        List<String> roles = rolesPart.isBlank()
                ? List.of()
                : Arrays.stream(rolesPart.split("~")).toList();

        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId.toString());
        claims.put("realm_access", Map.of("roles", roles));

        return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
    }

    private record SetupResult(CarModelResponse model, PartResponse part) {
    }
}