package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LocationRequest(
        @NotNull UUID warehouseId,
        UUID parentLocationId,
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @NotNull LocationType type
) {
}
