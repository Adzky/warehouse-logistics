package com.example.warehouse_logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "outbound_order_lines")
public class OutboundOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OutboundOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_location_id", nullable = false)
    private WarehouseLocation sourceLocation;

    @Column(nullable = false)
    private long quantity;

    protected OutboundOrderLine() {
    }

    public OutboundOrderLine(OutboundOrder order, Product product,
                             WarehouseLocation sourceLocation, long quantity) {
        this.order = order;
        this.product = product;
        this.sourceLocation = sourceLocation;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public WarehouseLocation getSourceLocation() { return sourceLocation; }
    public long getQuantity() { return quantity; }
}
