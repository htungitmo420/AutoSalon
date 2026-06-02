package org.example.paymentservice.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.commoncontracts.grpc.payment.GetPayableQuoteRequest;
import org.example.commoncontracts.grpc.payment.PayableQuoteResponse;
import org.example.commoncontracts.grpc.payment.PayableQuoteServiceGrpc;
import org.example.commoncontracts.payment.PaymentTargetType;
import org.example.paymentservice.application.port.PayableQuoteClient;
import org.example.paymentservice.domain.exceptions.DomainValidationException;
import org.example.paymentservice.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OrderPayableQuoteGrpcClient implements PayableQuoteClient {
    @GrpcClient("order-service")
    private PayableQuoteServiceGrpc.PayableQuoteServiceBlockingStub stub;

    @Value("${order.grpc.timeout-millis:2000}")
    private long timeoutMillis;

    public PayableQuote getQuote(PaymentTargetType targetType, UUID referenceId, UUID customerId) {
        try {
            PayableQuoteResponse response = stub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .getPayableQuote(GetPayableQuoteRequest.newBuilder()
                            .setTargetType(targetType.name())
                            .setReferenceId(referenceId.toString())
                            .setCustomerId(customerId.toString())
                            .build());
            return new PayableQuote(
                    PaymentTargetType.valueOf(response.getTargetType()),
                    UUID.fromString(response.getReferenceId()),
                    UUID.fromString(response.getCustomerId()),
                    new BigDecimal(response.getTotalAmount()),
                    response.getActive());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new EntityNotFoundException("Payable reference not found");
            }
            throw new DomainValidationException("Payable reference is unavailable");
        }
    }
}
