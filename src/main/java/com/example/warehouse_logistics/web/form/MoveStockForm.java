package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.StockMoveRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class MoveStockForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID sourceLocationId;

    @NotNull
    private UUID destinationLocationId;

    @Positive
    private Long quantity;

    private String reference;

    public StockMoveRequest toRequest() {
        return new StockMoveRequest(productId, sourceLocationId, destinationLocationId, quantity, reference);
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

    public UUID getDestinationLocationId() {
        return destinationLocationId;
    }

    public void setDestinationLocationId(UUID destinationLocationId) {
        this.destinationLocationId = destinationLocationId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
