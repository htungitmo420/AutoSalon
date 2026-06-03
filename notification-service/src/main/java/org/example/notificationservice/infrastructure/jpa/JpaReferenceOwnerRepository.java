package org.example.notificationservice.infrastructure.jpa;

import org.example.notificationservice.domain.projection.ReferenceOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface JpaReferenceOwnerRepository extends JpaRepository<ReferenceOwner, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO auto_salon.reference_owners(reference_id, customer_id, reference_type, updated_at)
            VALUES (:referenceId, :customerId, :referenceType, :updatedAt)
            ON CONFLICT (reference_id) DO UPDATE
            SET customer_id = EXCLUDED.customer_id,
                reference_type = EXCLUDED.reference_type,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsert(
            @Param("referenceId") UUID referenceId,
            @Param("customerId") UUID customerId,
            @Param("referenceType") String referenceType,
            @Param("updatedAt") Instant updatedAt);
}
