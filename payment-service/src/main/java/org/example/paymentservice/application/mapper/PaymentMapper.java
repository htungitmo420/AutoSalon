package org.example.paymentservice.application.mapper;

import org.example.paymentservice.application.dto.PaymentIntentResponse;
import org.example.paymentservice.domain.payment.PaymentIntent;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentIntentResponse toResponse(PaymentIntent paymentIntent);
}
