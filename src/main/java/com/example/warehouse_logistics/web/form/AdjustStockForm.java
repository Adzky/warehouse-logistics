package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.StockAdjustmentRequest;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AdjustStockForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID locationId;

    @NotNull
    private Long delta;

    private String reference;

    public StockAdjustmentRequest toRequest() {
        return new StockAdjustmentRequest(productId, locationId, delta, reference);
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

    public Long getDelta() {
        return delta;
    }

    public void setDelta(Long delta) {
        this.delta = delta;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
