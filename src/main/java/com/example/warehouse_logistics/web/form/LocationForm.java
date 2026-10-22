package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.LocationRequest;
import com.example.warehouse_logistics.entity.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class LocationForm {

    @NotNull
    private UUID warehouseId;

    private UUID parentLocationId;

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private LocationType type;

    public LocationRequest toRequest() {
        return new LocationRequest(warehouseId, parentLocationId, code, name, type);
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(UUID warehouseId) {
        this.warehouseId = warehouseId;
    }

    public UUID getParentLocationId() {
        return parentLocationId;
    }

    public void setParentLocationId(UUID parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }
}
