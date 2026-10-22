package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.LocationType;
import com.example.warehouse_logistics.entity.WarehouseLocation;

import java.util.UUID;

public record LocationResponse(
        UUID id, UUID warehouseId, String warehouseCode,
        UUID parentLocationId, String parentLocationCode,
        String code, String name, LocationType type, boolean active
) {
    public static LocationResponse from(WarehouseLocation location) {
        return new LocationResponse(
                location.getId(),
                location.getWarehouse().getId(),
                location.getWarehouse().getCode(),
                location.getParentLocation() == null ? null : location.getParentLocation().getId(),
                location.getParentLocation() == null ? null : location.getParentLocation().getCode(),
                location.getCode(),
                location.getName(),
                location.getType(),
                location.isActive()
        );
    }
}
