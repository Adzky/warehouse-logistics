package com.example.warehouse_logistics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceiveRequest(@NotEmpty List<@Valid ReceiveLineRequest> lines) {
}
