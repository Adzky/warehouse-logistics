package com.example.warehouse_logistics.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull UUID productId,
        @NotNull UUID locationId,
        @NotNull Long delta,
        String reference
) {
}
