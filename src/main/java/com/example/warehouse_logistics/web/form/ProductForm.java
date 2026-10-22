package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.ProductRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductForm {

    @NotBlank
    @Size(max = 64)
    private String sku;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 32)
    private String unitOfMeasure = "each";

    public ProductRequest toRequest() {
        return new ProductRequest(sku, name, description, unitOfMeasure);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}
