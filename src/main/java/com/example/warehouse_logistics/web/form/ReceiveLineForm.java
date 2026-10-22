package com.example.warehouse_logistics.web.form;

import java.util.UUID;

public class ReceiveLineForm {

    private UUID lineId;
    private Long quantity;

    public UUID getLineId() {
        return lineId;
    }

    public void setLineId(UUID lineId) {
        this.lineId = lineId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
