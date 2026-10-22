package com.example.warehouse_logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private WarehouseLocation sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id")
    private WarehouseLocation destinationLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 32)
    private MovementType type;

    @Column(nullable = false)
    private long quantity;

    @Column(length = 120)
    private String reference;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StockMovement() {
    }

    public StockMovement(Product product, WarehouseLocation sourceLocation,
                         WarehouseLocation destinationLocation, MovementType type,
                         long quantity, String reference) {
        this.product = product;
        this.sourceLocation = sourceLocation;
        this.destinationLocation = destinationLocation;
        this.type = type;
        this.quantity = quantity;
        this.reference = reference;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public WarehouseLocation getSourceLocation() { return sourceLocation; }
    public WarehouseLocation getDestinationLocation() { return destinationLocation; }
    public MovementType getType() { return type; }
    public long getQuantity() { return quantity; }
    public String getReference() { return reference; }
    public Instant getOccurredAt() { return occurredAt; }
}
