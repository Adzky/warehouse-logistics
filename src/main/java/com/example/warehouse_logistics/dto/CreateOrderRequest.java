package com.example.warehouse_logistics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 120) String reference,
        @NotEmpty List<@Valid OrderLineRequest> lines
) {
}
