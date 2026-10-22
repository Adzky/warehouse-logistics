package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.OrderLineRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class OrderLineForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID sourceLocationId;

    @Positive
    private Long quantity;

    public OrderLineRequest toRequest() {
        return new OrderLineRequest(productId, sourceLocationId, quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getSourceLocationId() {
        return sourceLocationId;
    }

    public void setSourceLocationId(UUID sourceLocationId) {
        this.sourceLocationId = sourceLocationId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
