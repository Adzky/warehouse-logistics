package com.example.warehouse_logistics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record StockMoveRequest(
        @NotNull UUID productId,
        @NotNull UUID sourceLocationId,
        @NotNull UUID destinationLocationId,
        @Positive long quantity,
        String reference
) {
}
