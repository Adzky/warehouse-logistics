package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.InboundReceiptLine;

import java.util.UUID;

public record ReceiptLineResponse(
        UUID id, UUID productId, String sku, String productName,
        UUID destinationLocationId, String destinationLocationCode,
        long expectedQuantity, long receivedQuantity, long outstandingQuantity
) {
    public static ReceiptLineResponse from(InboundReceiptLine line) {
        return new ReceiptLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getProduct().getSku(),
                line.getProduct().getName(),
                line.getDestinationLocation().getId(),
                line.getDestinationLocation().getCode(),
                line.getExpectedQuantity(),
                line.getReceivedQuantity(),
                line.getExpectedQuantity() - line.getReceivedQuantity()
        );
    }
}
