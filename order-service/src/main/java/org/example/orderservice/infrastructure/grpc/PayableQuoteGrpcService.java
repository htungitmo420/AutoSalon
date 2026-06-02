package org.example.orderservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.commoncontracts.grpc.payment.GetPayableQuoteRequest;
import org.example.commoncontracts.grpc.payment.PayableQuoteResponse;
import org.example.commoncontracts.grpc.payment.PayableQuoteServiceGrpc;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.orderservice.application.service.PayableQuoteService;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class PayableQuoteGrpcService extends PayableQuoteServiceGrpc.PayableQuoteServiceImplBase {
    private final PayableQuoteService payableQuoteService;

    @Override
    public void getPayableQuote(GetPayableQuoteRequest request, StreamObserver<PayableQuoteResponse> observer) {
        try {
            PayableQuoteService.PayableQuote quote = payableQuoteService.getQuote(
                    PaymentTargetType.valueOf(request.getTargetType()),
                    UUID.fromString(request.getReferenceId()),
                    UUID.fromString(request.getCustomerId()));
            observer.onNext(PayableQuoteResponse.newBuilder()
                    .setTargetType(quote.targetType().name())
                    .setReferenceId(quote.referenceId().toString())
                    .setCustomerId(quote.customerId().toString())
                    .setTotalAmount(quote.totalAmount().toPlainString())
                    .setActive(quote.active())
                    .build());
            observer.onCompleted();
        } catch (EntityNotFoundException exception) {
            observer.onError(Status.NOT_FOUND.withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            observer.onError(Status.FAILED_PRECONDITION.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
