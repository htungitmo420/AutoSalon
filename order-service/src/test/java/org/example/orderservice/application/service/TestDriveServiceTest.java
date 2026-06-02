package org.example.orderservice.application.service;

import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.request.QuoteTestDriveRequest;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.example.orderservice.application.port.out.OrderWorkflowEventPublisher;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.example.orderservice.infrastructure.inmemory.InMemoryTestDriveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestDriveServiceTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CAR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDateTime TOMORROW = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime DAY_AFTER = LocalDateTime.now().plusDays(2);

    private TestDriveService testDriveService;

    @BeforeEach
    void setUp() {
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(currentUserProvider.hasElevatedReadAccess()).thenReturn(false);

        testDriveService = new TestDriveService(
                new InMemoryTestDriveRepository(),
                currentUserProvider,
                mock(OrderWorkflowEventPublisher.class));
    }

    private TestDriveResponse book(UUID carId, LocalDateTime dateTime) {
        return testDriveService.bookTestDrive(new BookTestDriveRequest(carId, UUID.randomUUID(), dateTime));
    }

    @Test
    void book_createsPendingBookingForExternalCarId() {
        TestDriveResponse response = book(CAR_ID, TOMORROW);

        assertEquals(CAR_ID, response.carId());
        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals(TestDriveStatus.PENDING, response.status());
        assertEquals(TOMORROW, response.startDateTime());
    }

    @Test
    void book_conflictingTimeSlot() {
        book(CAR_ID, TOMORROW);

        assertThrows(DomainValidationException.class, () -> book(CAR_ID, TOMORROW));
    }

    @Test
    void book_differentTimeSlotsAllowed() {
        book(CAR_ID, TOMORROW);

        assertDoesNotThrow(() -> book(CAR_ID, DAY_AFTER));
    }

    @Test
    void get_returnsTestDrive() {
        TestDriveResponse created = book(CAR_ID, TOMORROW);

        assertEquals(created.id(), testDriveService.getTestDrive(created.id()).id());
    }

    @Test
    void get_notFound() {
        assertThrows(EntityNotFoundException.class, () -> testDriveService.getTestDrive(UUID.randomUUID()));
    }

    @Test
    void list_returnsCurrentUserBookings() {
        book(CAR_ID, TOMORROW);
        book(CAR_ID, DAY_AFTER);

        assertEquals(2, testDriveService.listTestDrives().size());
    }

    @Test
    void delete_removesBooking() {
        TestDriveResponse testDrive = book(CAR_ID, TOMORROW);

        testDriveService.deleteTestDrive(testDrive.id());

        assertThrows(EntityNotFoundException.class, () -> testDriveService.getTestDrive(testDrive.id()));
    }

    @Test
    void delete_notFound() {
        assertThrows(EntityNotFoundException.class, () -> testDriveService.deleteTestDrive(UUID.randomUUID()));
    }

    @Test
    void statusTransitions_confirmAndComplete() {
        TestDriveResponse testDrive = book(CAR_ID, TOMORROW);

        testDrive = testDriveService.quoteTestDrive(testDrive.id(), new QuoteTestDriveRequest(java.math.BigDecimal.TEN));
        assertEquals(TestDriveStatus.WAITING_FOR_PAYMENT, testDrive.status());

        testDriveService.handlePaymentSucceeded(testDrive.id());
        testDrive = testDriveService.getTestDrive(testDrive.id());
        assertEquals(TestDriveStatus.CONFIRMED, testDrive.status());

        testDrive = testDriveService.completeTestDrive(testDrive.id());
        assertEquals(TestDriveStatus.COMPLETED, testDrive.status());
    }

    @Test
    void statusTransitions_cancel() {
        TestDriveResponse testDrive = testDriveService.cancelTestDrive(book(CAR_ID, TOMORROW).id());

        assertEquals(TestDriveStatus.CANCELLED, testDrive.status());
    }

    @Test
    void statusTransitions_invalidTransition() {
        TestDriveResponse testDrive = book(CAR_ID, TOMORROW);

        assertThrows(DomainValidationException.class, () -> testDriveService.completeTestDrive(testDrive.id()));
    }
}
