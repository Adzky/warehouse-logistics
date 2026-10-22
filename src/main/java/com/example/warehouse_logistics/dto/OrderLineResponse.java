package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.OutboundOrderLine;

import java.util.UUID;

public record OrderLineResponse(
        UUID productId, String sku, String productName,
        UUID sourceLocationId, String sourceLocationCode, long quantity
) {
    public static OrderLineResponse from(OutboundOrderLine line) {
        return new OrderLineResponse(
                line.getProduct().getId(),
                line.getProduct().getSku(),
                line.getProduct().getName(),
                line.getSourceLocation().getId(),
                line.getSourceLocation().getCode(),
                line.getQuantity()
        );
    }
}
