package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.WarehouseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WarehouseForm {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String location;

    public WarehouseRequest toRequest() {
        return new WarehouseRequest(code, name, location);
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
