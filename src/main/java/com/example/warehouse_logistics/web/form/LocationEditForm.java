package com.example.warehouse_logistics.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LocationEditForm {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

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
}
