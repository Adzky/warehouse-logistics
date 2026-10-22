package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.Product;

import java.util.UUID;

public record ProductResponse(
        UUID id, String sku, String name, String description,
        String unitOfMeasure, boolean active
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(), product.getSku(), product.getName(),
                product.getDescription(), product.getUnitOfMeasure(), product.isActive());
    }
}
