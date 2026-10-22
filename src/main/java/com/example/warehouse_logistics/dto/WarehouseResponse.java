package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.Warehouse;

import java.util.UUID;

public record WarehouseResponse(UUID id, String code, String name, String location) {

    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation()
        );
    }
}
