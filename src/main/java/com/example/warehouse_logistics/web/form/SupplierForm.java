package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.SupplierRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierForm {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Email
    @Size(max = 255)
    private String contactEmail;

    public SupplierRequest toRequest() {
        return new SupplierRequest(code, name, contactEmail);
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
