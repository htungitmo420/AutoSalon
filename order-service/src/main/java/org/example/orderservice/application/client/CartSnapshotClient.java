package org.example.orderservice.application.client;

import org.example.orderservice.application.dto.response.CartSnapshotResponse;

import java.util.UUID;

public interface CartSnapshotClient {

    CartSnapshotResponse lockCart(UUID cartId, UUID customerId);

    CartSnapshotResponse markCheckedOut(UUID cartId, UUID customerId);

    CartSnapshotResponse releaseCart(UUID cartId, UUID customerId);
}
