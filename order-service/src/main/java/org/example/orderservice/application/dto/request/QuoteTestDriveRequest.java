package org.example.orderservice.application.dto.request;

import java.math.BigDecimal;

public record QuoteTestDriveRequest(
        BigDecimal fee
) {
}
