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
@Table(name = "inbound_receipt_lines")
public class InboundReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    private InboundReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_location_id", nullable = false)
    private WarehouseLocation destinationLocation;

    @Column(name = "expected_quantity", nullable = false)
    private long expectedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private long receivedQuantity;

    protected InboundReceiptLine() {
    }

    public InboundReceiptLine(InboundReceipt receipt, Product product,
                              WarehouseLocation destinationLocation, long expectedQuantity) {
        this.receipt = receipt;
        this.product = product;
        this.destinationLocation = destinationLocation;
        this.expectedQuantity = expectedQuantity;
    }

    public void receive(long quantity) {
        if (quantity > expectedQuantity - receivedQuantity) {
            throw new IllegalArgumentException("Receipt quantity exceeds the expected quantity");
        }
        receivedQuantity = Math.addExact(receivedQuantity, quantity);
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public WarehouseLocation getDestinationLocation() { return destinationLocation; }
    public long getExpectedQuantity() { return expectedQuantity; }
    public long getReceivedQuantity() { return receivedQuantity; }
}
