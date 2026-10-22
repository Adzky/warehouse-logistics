package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.ReorderPointRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public class ReorderPointForm {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID locationId;

    @NotNull
    @PositiveOrZero
    private Long reorderPoint;

    public ReorderPointRequest toRequest() {
        return new ReorderPointRequest(productId, locationId, reorderPoint);
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

    public Long getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Long reorderPoint) {
        this.reorderPoint = reorderPoint;
    }
}
