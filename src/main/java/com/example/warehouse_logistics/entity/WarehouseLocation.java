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
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "warehouse_locations", uniqueConstraints =
        @UniqueConstraint(name = "uq_warehouse_location_code", columnNames = {"warehouse_id", "code"}))
public class WarehouseLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_location_id")
    private WarehouseLocation parentLocation;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 32)
    private LocationType type;

    @Column(nullable = false)
    private boolean active = true;

    protected WarehouseLocation() {
    }

    public WarehouseLocation(Warehouse warehouse, WarehouseLocation parentLocation,
                             String code, String name, LocationType type) {
        this.warehouse = warehouse;
        this.parentLocation = parentLocation;
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public UUID getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public WarehouseLocation getParentLocation() { return parentLocation; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public LocationType getType() { return type; }
    public boolean isActive() { return active; }
}
