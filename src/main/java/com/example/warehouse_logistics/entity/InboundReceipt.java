package com.example.warehouse_logistics.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inbound_receipts")
public class InboundReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReceiptStatus status = ReceiptStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundReceiptLine> lines = new ArrayList<>();

    protected InboundReceipt() {
    }

    public InboundReceipt(String reference, Supplier supplier) {
        this.reference = reference;
        this.supplier = supplier;
    }

    public void addLine(InboundReceiptLine line) {
        lines.add(line);
    }

    public void updateStatus() {
        boolean anyReceived = lines.stream().anyMatch(line -> line.getReceivedQuantity() > 0);
        boolean allReceived = lines.stream()
                .allMatch(line -> line.getReceivedQuantity() == line.getExpectedQuantity());
        status = allReceived ? ReceiptStatus.RECEIVED
                : anyReceived ? ReceiptStatus.PARTIALLY_RECEIVED : ReceiptStatus.OPEN;
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public Supplier getSupplier() { return supplier; }
    public ReceiptStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<InboundReceiptLine> getLines() { return lines; }
}
