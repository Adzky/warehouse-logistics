package com.example.warehouse_logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String contactEmail;

    @Column(nullable = false)
    private boolean active = true;

    protected Supplier() {
    }

    public Supplier(String code, String name, String contactEmail) {
        this.code = code;
        this.name = name;
        this.contactEmail = contactEmail;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getContactEmail() { return contactEmail; }
    public boolean isActive() { return active; }
    public void update(String code, String name, String contactEmail) {
        this.code = code;
        this.name = name;
        this.contactEmail = contactEmail;
    }
    public void setActive(boolean active) { this.active = active; }
}
