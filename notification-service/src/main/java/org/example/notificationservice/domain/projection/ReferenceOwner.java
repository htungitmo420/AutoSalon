package org.example.notificationservice.domain.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reference_owners", schema = "auto_salon")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceOwner {

    @Id
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "reference_type", nullable = false)
    private String referenceType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(UUID ownerId, String type, Instant now) {
        customerId = ownerId;
        referenceType = type;
        updatedAt = now;
    }
}
