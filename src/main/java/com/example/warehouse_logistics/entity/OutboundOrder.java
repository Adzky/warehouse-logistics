package com.example.warehouse_logistics.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "outbound_orders")
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OutboundOrderStatus status = OutboundOrderStatus.RESERVED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderLine> lines = new ArrayList<>();

    protected OutboundOrder() {
    }

    public OutboundOrder(String reference) {
        this.reference = reference;
    }

    public void addLine(OutboundOrderLine line) {
        lines.add(line);
    }

    public void markPicked() {
        if (status != OutboundOrderStatus.RESERVED) {
            throw new IllegalStateException("Only reserved orders can be picked");
        }
        status = OutboundOrderStatus.PICKED;
    }

    public void markShipped() {
        if (status != OutboundOrderStatus.PACKED) {
            throw new IllegalStateException("Only packed orders can be shipped");
        }
        status = OutboundOrderStatus.SHIPPED;
    }

    public void cancel() {
        if (status != OutboundOrderStatus.RESERVED) {
            throw new IllegalStateException("Only reserved orders can be cancelled");
        }
        status = OutboundOrderStatus.CANCELLED;
    }

    public void markPacked() {
        if (status != OutboundOrderStatus.PICKED) {
            throw new IllegalStateException("Only picked orders can be packed");
        }
        status = OutboundOrderStatus.PACKED;
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public OutboundOrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OutboundOrderLine> getLines() { return lines; }
}
