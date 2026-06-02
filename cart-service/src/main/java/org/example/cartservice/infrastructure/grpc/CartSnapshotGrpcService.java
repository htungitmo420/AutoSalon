package org.example.cartservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.cartservice.application.service.CartService;
import org.example.cartservice.domain.exceptions.DomainValidationException;
import org.example.cartservice.domain.exceptions.EntityNotFoundException;
import org.example.cartservice.infrastructure.logging.TraceContext;
import org.example.commoncontracts.grpc.cart.CartCommandRequest;
import org.example.commoncontracts.grpc.cart.CartSnapshotResponse;
import org.example.commoncontracts.grpc.cart.CartSnapshotServiceGrpc;
import org.example.commoncontracts.grpc.cart.LockCartRequest;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CartSnapshotGrpcService extends CartSnapshotServiceGrpc.CartSnapshotServiceImplBase {

    private final CartService cartService;

    @Override
    public void lockCart(LockCartRequest request, StreamObserver<CartSnapshotResponse> observer) {
        execute(request.getTraceId(), observer, () -> cartService.lockCart(
                UUID.fromString(request.getCartId()), UUID.fromString(request.getCustomerId())));
    }

    @Override
    public void markCartCheckedOut(CartCommandRequest request, StreamObserver<CartSnapshotResponse> observer) {
        execute(request.getTraceId(), observer, () -> cartService.markCheckedOut(
                UUID.fromString(request.getCartId()), UUID.fromString(request.getCustomerId())));
    }

    @Override
    public void releaseCart(CartCommandRequest request, StreamObserver<CartSnapshotResponse> observer) {
        execute(request.getTraceId(), observer, () -> cartService.releaseCart(
                UUID.fromString(request.getCartId()), UUID.fromString(request.getCustomerId())));
    }

    private void execute(String traceId, StreamObserver<CartSnapshotResponse> observer,
                         Supplier<org.example.cartservice.domain.cart.model.Cart> operation) {
        TraceContext.runWithTraceId(traceId, () -> {
            try {
                observer.onNext(CartSnapshotGrpcMapper.toResponse(operation.get()));
                observer.onCompleted();
            } catch (EntityNotFoundException exception) {
                observer.onError(Status.NOT_FOUND.withDescription(exception.getMessage()).asRuntimeException());
            } catch (DomainValidationException | IllegalArgumentException exception) {
                observer.onError(Status.FAILED_PRECONDITION.withDescription(exception.getMessage()).asRuntimeException());
            } catch (RuntimeException exception) {
                log.error("Cart snapshot gRPC operation failed", exception);
                observer.onError(Status.INTERNAL.withDescription("Cart snapshot operation failed").asRuntimeException());
            }
        });
    }
}
