package org.example.rest;

import org.example.orderservice.OrderServiceApplication;
import org.example.orderservice.application.client.InventoryReservationClient;
import org.example.orderservice.application.dto.request.LoginRequest;
import org.example.orderservice.application.dto.request.RegisterRequest;
import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.AuthResponse;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.dto.response.InventoryReservationResponse;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = OrderServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Testcontainers
class RestApiIT {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-100000000001");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-100000000002");
    private static final UUID WAREHOUSE_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-100000000003");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-100000000004");

    private static final UUID EXTERNAL_CAR_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID EXTERNAL_MODEL_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID EXTERNAL_PART_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

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
    WebTestClient webTestClient;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    InventoryReservationClient inventoryReservationClient;

    @Autowired
    OrderService orderService;

    @BeforeEach
    void setUpJwtDecoder() {
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> jwtForToken(invocation.getArgument(0)));
        when(inventoryReservationClient.reserveStockCar(any(), any(), any()))
                .thenAnswer(invocation -> reservation(BigDecimal.valueOf(3550000)));
        when(inventoryReservationClient.reserveConfiguration(any(), any(), any(), any()))
                .thenAnswer(invocation -> reservation(BigDecimal.valueOf(3420000)));
        when(inventoryReservationClient.confirmReservation(any(), any()))
                .thenAnswer(invocation -> reservation(BigDecimal.valueOf(3420000)));
    }

    @Nested
    @DisplayName("Auth")
    class AuthTests {

        @Test
        void registerAndLogin() {
            String email = "user-" + UUID.randomUUID() + "@example.com";
            String password = UUID.randomUUID() + Character.toString('!') + "Aa1";

            AuthResponse registered = webTestClient.post().uri("/api/auth/register")
                    .bodyValue(new RegisterRequest(email, password, "Demo User"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AuthResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(registered);
            assertEquals(email, registered.email());
            assertEquals(List.of("USER"), registered.roles());
            assertNotNull(registered.accessToken());

            AuthResponse loggedIn = webTestClient.post().uri("/api/auth/login")
                    .bodyValue(new LoginRequest(email, password))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(loggedIn);
            assertEquals(registered.userId(), loggedIn.userId());
            assertNotNull(loggedIn.accessToken());
        }
    }

    @Nested
    @DisplayName("Common Orders")
    class CommonOrderTests {

        @Test
        void fullLifecycle() {
            CommonOrderResponse order = placeCommonOrder();
            assertEquals(CommonOrderStatus.WAITING_FOR_PAYMENT, order.status());
            assertEquals(USER_ID, order.customerId());

            order = patchCommon(order.id(), "mark-paid");
            assertEquals(CommonOrderStatus.PAID, order.status());

            order = patchCommon(order.id(), "ready-for-pickup");
            assertEquals(CommonOrderStatus.READY_FOR_PICKUP, order.status());

            order = patchCommon(order.id(), "complete");
            assertEquals(CommonOrderStatus.COMPLETED, order.status());
        }

        @Test
        void cancel() {
            CommonOrderResponse order = placeCommonOrder();
            order = patchCommon(order.id(), "cancel");

            assertEquals(CommonOrderStatus.CANCELLED, order.status());
        }

        @Test
        void getAndListOnlyOwnOrdersForUser() {
            CommonOrderResponse order = placeCommonOrder();

            CommonOrderResponse got = userClient().get().uri("/api/orders/common/{id}", order.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CommonOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(order.id(), got.id());

            List<CommonOrderResponse> list = userClient().get().uri("/api/orders/common")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(CommonOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(list);
            assertTrue(list.stream().anyMatch(x -> x.id().equals(order.id())));
            assertFalse(list.stream().anyMatch(x -> !x.customerId().equals(USER_ID)));
        }

        @Test
        void deleteByAdmin() {
            CommonOrderResponse order = placeCommonOrder();

            adminClient().delete().uri("/api/orders/common/{id}", order.id())
                    .exchange()
                    .expectStatus().isNoContent();

            managerClient().get().uri("/api/orders/common/{id}", order.id())
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void invalidTransitionReturnsBadRequest() {
            CommonOrderResponse order = placeCommonOrder();

            managerClient().patch().uri("/api/orders/common/{id}/approve", order.id())
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    @DisplayName("Custom Orders")
    class CustomOrderTests {

        @Test
        void fullLifecycle() {
            CustomOrderResponse order = placeCustomOrder();
            assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT, order.status());
            assertEquals(USER_ID, order.customerId());
            assertEquals(Map.of("WHEELS", EXTERNAL_PART_ID), order.selectedPartIds());
            assertEquals(BigDecimal.valueOf(3420000), order.totalPrice());

            order = patchCustom(order.id(), "mark-paid");
            assertEquals(CustomOrderStatus.ASSEMBLING, order.status());

            orderService.handleAssemblyCompleted(order.id());
            order = managerClient().get().uri("/api/orders/custom/{id}", order.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CustomOrderResponse.class)
                    .returnResult().getResponseBody();
            assertEquals(CustomOrderStatus.READY_FOR_PICKUP, order.status());

            order = patchCustom(order.id(), "complete");
            assertEquals(CustomOrderStatus.COMPLETED, order.status());
        }

        @Test
        void cancel() {
            CustomOrderResponse order = placeCustomOrder();
            order = patchCustom(order.id(), "cancel");

            assertEquals(CustomOrderStatus.CANCELLED, order.status());
        }

        @Test
        void getAndListOnlyOwnOrdersForUser() {
            CustomOrderResponse order = placeCustomOrder();

            CustomOrderResponse got = userClient().get().uri("/api/orders/custom/{id}", order.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CustomOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(order.id(), got.id());

            List<CustomOrderResponse> list = userClient().get().uri("/api/orders/custom")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(CustomOrderResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(list);
            assertTrue(list.stream().anyMatch(x -> x.id().equals(order.id())));
            assertFalse(list.stream().anyMatch(x -> !x.customerId().equals(USER_ID)));
        }

        @Test
        void deleteByAdmin() {
            CustomOrderResponse order = placeCustomOrder();

            adminClient().delete().uri("/api/orders/custom/{id}", order.id())
                    .exchange()
                    .expectStatus().isNoContent();

            managerClient().get().uri("/api/orders/custom/{id}", order.id())
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void invalidTransitionReturnsBadRequest() {
            CustomOrderResponse order = placeCustomOrder();

            warehouseClient().patch().uri("/api/orders/custom/{id}/approve", order.id())
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    @DisplayName("Test Drives")
    class TestDriveTests {

        @Test
        void fullLifecycle() {
            TestDriveResponse testDrive = bookTestDrive(LocalDateTime.of(2026, 8, 1, 10, 0));
            assertEquals(TestDriveStatus.PENDING, testDrive.status());
            assertEquals(USER_ID, testDrive.customerId());

            testDrive = patchTestDrive(testDrive.id(), "confirm");
            assertEquals(TestDriveStatus.CONFIRMED, testDrive.status());

            testDrive = patchTestDrive(testDrive.id(), "complete");
            assertEquals(TestDriveStatus.COMPLETED, testDrive.status());
        }

        @Test
        void cancel() {
            TestDriveResponse testDrive = bookTestDrive(LocalDateTime.of(2026, 8, 2, 10, 0));
            testDrive = patchTestDrive(testDrive.id(), "cancel");

            assertEquals(TestDriveStatus.CANCELLED, testDrive.status());
        }

        @Test
        void getAndListOnlyOwnBookingsForUser() {
            TestDriveResponse testDrive = bookTestDrive(LocalDateTime.of(2026, 8, 3, 10, 0));

            TestDriveResponse got = userClient().get().uri("/api/test-drives/{id}", testDrive.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(TestDriveResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(got);
            assertEquals(testDrive.id(), got.id());

            List<TestDriveResponse> list = userClient().get().uri("/api/test-drives")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(TestDriveResponse.class)
                    .returnResult().getResponseBody();

            assertNotNull(list);
            assertTrue(list.stream().anyMatch(t -> t.id().equals(testDrive.id())));
            assertFalse(list.stream().anyMatch(t -> !t.customerId().equals(USER_ID)));
        }

        @Test
        void deleteByOwner() {
            TestDriveResponse testDrive = bookTestDrive(LocalDateTime.of(2026, 8, 4, 10, 0));

            userClient().delete().uri("/api/test-drives/{id}", testDrive.id())
                    .exchange()
                    .expectStatus().isNoContent();

            managerClient().get().uri("/api/test-drives/{id}", testDrive.id())
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void duplicateActiveBookingReturnsBadRequest() {
            LocalDateTime startDateTime = LocalDateTime.of(2026, 8, 5, 10, 0);
            bookTestDrive(startDateTime);

            userClient().post().uri("/api/test-drives")
                    .bodyValue(new BookTestDriveRequest(EXTERNAL_CAR_ID, USER_ID, startDateTime))
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    private CommonOrderResponse placeCommonOrder() {
        return userClient().post().uri("/api/orders/common")
                .bodyValue(new CommonOrderRequest(EXTERNAL_CAR_ID, USER_ID))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommonOrderResponse.class)
                .returnResult().getResponseBody();
    }

    private CustomOrderResponse placeCustomOrder() {
        return userClient().post().uri("/api/orders/custom")
                .bodyValue(new CustomOrderRequest(
                        EXTERNAL_MODEL_ID,
                        USER_ID,
                        Map.of("WHEELS", EXTERNAL_PART_ID)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomOrderResponse.class)
                .returnResult().getResponseBody();
    }

    private TestDriveResponse bookTestDrive(LocalDateTime dateTime) {
        return userClient().post().uri("/api/test-drives")
                .bodyValue(new BookTestDriveRequest(EXTERNAL_CAR_ID, USER_ID, dateTime))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TestDriveResponse.class)
                .returnResult().getResponseBody();
    }

    private CommonOrderResponse patchCommon(UUID orderId, String action) {
        WebTestClient.RequestHeadersSpec<?> request = switch (action) {
            case "approve", "request-payment", "mark-paid", "ready-for-pickup", "complete" ->
                    managerClient().patch().uri("/api/orders/common/{id}/{action}", orderId, action);
            case "cancel" ->
                    userClient().patch().uri("/api/orders/common/{id}/{action}", orderId, action);
            default ->
                    throw new IllegalArgumentException("Unsupported common order action: " + action);
        };

        return request
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommonOrderResponse.class)
                .returnResult().getResponseBody();
    }

    private CustomOrderResponse patchCustom(UUID orderId, String action) {
        WebTestClient.RequestHeadersSpec<?> request = switch (action) {
            case "approve", "waiting-for-delivery", "ready-for-pickup" ->
                    warehouseClient().patch().uri("/api/orders/custom/{id}/{action}", orderId, action);
            case "request-payment", "mark-paid", "complete" ->
                    managerClient().patch().uri("/api/orders/custom/{id}/{action}", orderId, action);
            case "cancel" ->
                    userClient().patch().uri("/api/orders/custom/{id}/{action}", orderId, action);
            default ->
                    throw new IllegalArgumentException("Unsupported custom order action: " + action);
        };

        return request
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomOrderResponse.class)
                .returnResult().getResponseBody();
    }

    private TestDriveResponse patchTestDrive(UUID testDriveId, String action) {
        WebTestClient.RequestHeadersSpec<?> request = switch (action) {
            case "confirm", "complete" ->
                    managerClient().patch().uri("/api/test-drives/{id}/{action}", testDriveId, action);
            case "cancel" ->
                    userClient().patch().uri("/api/test-drives/{id}/{action}", testDriveId, action);
            default ->
                    throw new IllegalArgumentException("Unsupported test-drive action: " + action);
        };

        return request
                .exchange()
                .expectStatus().isOk()
                .expectBody(TestDriveResponse.class)
                .returnResult().getResponseBody();
    }

    private WebTestClient userClient() {
        return authorizedClient(USER_ID, "USER");
    }

    private WebTestClient managerClient() {
        return authorizedClient(MANAGER_ID, "MANAGER");
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
        claims.put("roles", roles);

        return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
    }

    private InventoryReservationResponse reservation(BigDecimal price) {
        return new InventoryReservationResponse(
                UUID.randomUUID(), "HELD", Instant.now().plusSeconds(900), price);
    }
}
