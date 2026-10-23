package com.example.warehouse_logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "unit_of_measure", nullable = false, length = 32)
    private String unitOfMeasure;

    @Column(nullable = false)
    private boolean active = true;

    protected Product() {
    }

    public Product(String sku, String name, String description, String unitOfMeasure) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public boolean isActive() { return active; }
    public void update(String sku, String name, String description, String unitOfMeasure) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
    }
    public void setActive(boolean active) { this.active = active; }
}
