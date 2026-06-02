package org.example.orderservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.commoncontracts.grpc.cart.CartSnapshotServiceGrpc;
import org.example.orderservice.application.client.CartSnapshotClient;
import org.example.orderservice.application.dto.response.CartSnapshotResponse;
import org.example.orderservice.application.exception.CartServiceUnavailableException;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CartSnapshotGrpcClient implements CartSnapshotClient {

    @GrpcClient("cart-service")
    private CartSnapshotServiceGrpc.CartSnapshotServiceBlockingStub cartStub;

    @Value("${cart.grpc.timeout-millis:2000}")
    private long timeoutMillis;

    @Override
    public CartSnapshotResponse lockCart(UUID cartId, UUID customerId) {
        try {
            return CartSnapshotGrpcMapper.toResponse(stub().lockCart(
                    CartSnapshotGrpcMapper.toLockRequest(cartId, customerId, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException exception) {
            throw mapException(exception, "Failed to lock cart");
        }
    }

    @Override
    public CartSnapshotResponse markCheckedOut(UUID cartId, UUID customerId) {
        try {
            return CartSnapshotGrpcMapper.toResponse(stub().markCartCheckedOut(
                    CartSnapshotGrpcMapper.toCommandRequest(cartId, customerId, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException exception) {
            throw mapException(exception, "Failed to mark cart as checked out");
        }
    }

    @Override
    public CartSnapshotResponse releaseCart(UUID cartId, UUID customerId) {
        try {
            return CartSnapshotGrpcMapper.toResponse(stub().releaseCart(
                    CartSnapshotGrpcMapper.toCommandRequest(cartId, customerId, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException exception) {
            throw mapException(exception, "Failed to release cart");
        }
    }

    private CartSnapshotServiceGrpc.CartSnapshotServiceBlockingStub stub() {
        return cartStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    private RuntimeException mapException(StatusRuntimeException exception, String fallbackMessage) {
        Status status = exception.getStatus();
        String description = status.getDescription() == null ? fallbackMessage : status.getDescription();
        return switch (status.getCode()) {
            case NOT_FOUND -> new EntityNotFoundException(description);
            case INVALID_ARGUMENT, FAILED_PRECONDITION -> new DomainValidationException(description);
            default -> new CartServiceUnavailableException(fallbackMessage, exception);
        };
    }
}
