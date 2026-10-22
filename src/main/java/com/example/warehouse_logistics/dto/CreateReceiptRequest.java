package com.example.warehouse_logistics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateReceiptRequest(
        @NotBlank @Size(max = 120) String reference,
        UUID supplierId,
        @NotEmpty List<@Valid ReceiptLineRequest> lines
) {
}
