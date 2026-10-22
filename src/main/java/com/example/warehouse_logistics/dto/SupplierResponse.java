package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.Supplier;

import java.util.UUID;

public record SupplierResponse(UUID id, String code, String name, String contactEmail) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(), supplier.getCode(), supplier.getName(), supplier.getContactEmail());
    }
}
