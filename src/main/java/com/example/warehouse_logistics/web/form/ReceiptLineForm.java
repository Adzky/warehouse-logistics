package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.ReceiptLineRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class ReceiptLineForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID destinationLocationId;

    @Positive
    private Long expectedQuantity;

    public ReceiptLineRequest toRequest() {
        return new ReceiptLineRequest(productId, destinationLocationId, expectedQuantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getDestinationLocationId() {
        return destinationLocationId;
    }

    public void setDestinationLocationId(UUID destinationLocationId) {
        this.destinationLocationId = destinationLocationId;
    }

    public Long getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(Long expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }
}
