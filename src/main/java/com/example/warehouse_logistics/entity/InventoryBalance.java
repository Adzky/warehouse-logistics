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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "inventory_balances", uniqueConstraints =
        @UniqueConstraint(name = "uq_inventory_product_location",
                columnNames = {"product_id", "location_id"}))
public class InventoryBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private WarehouseLocation location;

    @Column(name = "on_hand", nullable = false)
    private long onHand;

    @Column(nullable = false)
    private long reserved;

    @Column(name = "reorder_point", nullable = false)
    private long reorderPoint;

    @Version
    @Column(nullable = false)
    private long version;

    protected InventoryBalance() {
    }

    public InventoryBalance(Product product, WarehouseLocation location) {
        this.product = product;
        this.location = location;
    }

    public void receive(long quantity) {
        onHand = Math.addExact(onHand, quantity);
    }

    public void reserve(long quantity) {
        if (available() < quantity) {
            throw new IllegalStateException("Insufficient available inventory");
        }
        reserved = Math.addExact(reserved, quantity);
    }

    public void pick(long quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("Insufficient reserved inventory");
        }
    }

    public void ship(long quantity) {
        if (reserved < quantity || onHand < quantity) {
            throw new IllegalStateException("Insufficient reserved inventory to ship");
        }
        reserved -= quantity;
        onHand -= quantity;
    }

    public void release(long quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("Insufficient reserved inventory to release");
        }
        reserved -= quantity;
    }

    public void adjust(long delta) {
        long newOnHand = Math.addExact(onHand, delta);
        if (delta < 0 && newOnHand < reserved) {
            throw new IllegalStateException("Adjustment cannot reduce stock below reserved quantity");
        }
        if (newOnHand < 0) {
            throw new IllegalStateException("Adjustment cannot make on-hand stock negative");
        }
        onHand = newOnHand;
    }

    public long available() {
        return onHand - reserved;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public WarehouseLocation getLocation() { return location; }
    public long getOnHand() { return onHand; }
    public long getReserved() { return reserved; }
    public long getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(long reorderPoint) { this.reorderPoint = reorderPoint; }
}
