package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.InboundReceipt;
import com.example.warehouse_logistics.entity.ReceiptStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReceiptResponse(
        UUID id, String reference, UUID supplierId, String supplierName, ReceiptStatus status,
        Instant createdAt, List<ReceiptLineResponse> lines
) {
    public static ReceiptResponse from(InboundReceipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getReference(),
                receipt.getSupplier() == null ? null : receipt.getSupplier().getId(),
                receipt.getSupplier() == null ? null : receipt.getSupplier().getName(),
                receipt.getStatus(),
                receipt.getCreatedAt(),
                receipt.getLines().stream().map(ReceiptLineResponse::from).toList()
        );
    }
}
