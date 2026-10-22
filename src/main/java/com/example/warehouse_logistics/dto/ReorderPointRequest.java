package com.example.warehouse_logistics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record ReorderPointRequest(
        @NotNull UUID productId,
        @NotNull UUID locationId,
        @NotNull @PositiveOrZero Long reorderPoint
) {
}
