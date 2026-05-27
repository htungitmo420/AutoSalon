package org.example.orderservice.domain.testdrive.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orderservice.domain.BaseEntity;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "test_drives", schema = "auto_salon")
public class TestDrive extends BaseEntity {

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestDriveStatus status;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;
}