package com.example.warehouse_logistics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record StockReceiptRequest(
        @NotNull UUID productId,
        @NotNull UUID locationId,
        @Positive long quantity,
        String reference
) {
}
