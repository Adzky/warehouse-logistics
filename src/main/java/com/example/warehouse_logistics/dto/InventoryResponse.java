package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.InventoryBalance;

import java.util.UUID;

public record InventoryResponse(
        UUID id, UUID productId, String sku, String productName,
        UUID warehouseId, String warehouseCode,
        UUID locationId, String locationCode, long onHand,
        long reserved, long available, long reorderPoint, boolean belowReorderPoint
) {
    public static InventoryResponse from(InventoryBalance balance) {
        return new InventoryResponse(
                balance.getId(),
                balance.getProduct().getId(),
                balance.getProduct().getSku(),
                balance.getProduct().getName(),
                balance.getLocation().getWarehouse().getId(),
                balance.getLocation().getWarehouse().getCode(),
                balance.getLocation().getId(),
                balance.getLocation().getCode(),
                balance.getOnHand(),
                balance.getReserved(),
                balance.available(),
                balance.getReorderPoint(),
                balance.available() <= balance.getReorderPoint()
        );
    }
}
