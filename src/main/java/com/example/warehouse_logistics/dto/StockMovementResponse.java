package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.MovementType;
import com.example.warehouse_logistics.entity.StockMovement;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id, UUID productId, String sku, MovementType type, long quantity,
        UUID sourceLocationId, String sourceLocationCode,
        UUID destinationLocationId, String destinationLocationCode,
        String reference, String performedBy, Instant occurredAt
) {
    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getType(),
                movement.getQuantity(),
                movement.getSourceLocation() == null ? null : movement.getSourceLocation().getId(),
                movement.getSourceLocation() == null ? null : movement.getSourceLocation().getCode(),
                movement.getDestinationLocation() == null ? null : movement.getDestinationLocation().getId(),
                movement.getDestinationLocation() == null ? null : movement.getDestinationLocation().getCode(),
                movement.getReference(),
                movement.getPerformedBy(),
                movement.getOccurredAt()
        );
    }
}
