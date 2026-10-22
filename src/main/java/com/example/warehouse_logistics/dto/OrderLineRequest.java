package com.example.warehouse_logistics.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderLineRequest(
        @NotNull UUID productId,
        @NotNull UUID sourceLocationId,
        @Positive long quantity
) {
}
