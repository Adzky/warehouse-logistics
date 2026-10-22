package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.StockReceiptRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class DirectReceiveForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID locationId;

    @Positive
    private Long quantity;

    private String reference;

    public StockReceiptRequest toRequest() {
        return new StockReceiptRequest(productId, locationId, quantity, reference);
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
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
