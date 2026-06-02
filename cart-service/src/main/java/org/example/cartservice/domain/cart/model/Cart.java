package org.example.cartservice.domain.cart.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.cartservice.domain.BaseEntity;
import org.example.cartservice.domain.cart.enums.CartItemType;
import org.example.cartservice.domain.cart.enums.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts", schema = "auto_salon")
public class Cart extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private CartItemType itemType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "model_id")
    private UUID modelId;

    @ElementCollection
    @CollectionTable(name = "cart_selected_parts", schema = "auto_salon",
            joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "part_type")
    @Column(name = "part_id", nullable = false)
    @Builder.Default
    private Map<String, UUID> selectedPartIds = new HashMap<>();

    @Column(name = "quoted_price", nullable = false)
    private BigDecimal quotedPrice;

    @Column(name = "quote_expires_at", nullable = false)
    private Instant quoteExpiresAt;

    @Version
    @Column(nullable = false)
    private long version;
}
