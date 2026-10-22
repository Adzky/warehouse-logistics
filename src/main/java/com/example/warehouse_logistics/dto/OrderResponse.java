package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.OutboundOrder;
import com.example.warehouse_logistics.entity.OutboundOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id, String reference, OutboundOrderStatus status,
        Instant createdAt, List<OrderLineResponse> lines
) {
    public static OrderResponse from(OutboundOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getLines().stream().map(OrderLineResponse::from).toList()
        );
    }
}
