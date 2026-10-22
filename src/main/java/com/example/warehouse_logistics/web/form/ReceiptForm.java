package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.CreateReceiptRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReceiptForm {

    @NotBlank
    @Size(max = 120)
    private String reference;

    private UUID supplierId;

    @Valid
    @NotEmpty
    private List<ReceiptLineForm> lines = new ArrayList<>(List.of(new ReceiptLineForm()));

    public CreateReceiptRequest toRequest() {
        return new CreateReceiptRequest(
                reference,
                supplierId,
                lines.stream().map(ReceiptLineForm::toRequest).toList());
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public List<ReceiptLineForm> getLines() {
        return lines;
    }

    public void setLines(List<ReceiptLineForm> lines) {
        this.lines = lines;
    }
}
