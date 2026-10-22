package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class OrderForm {

    @NotBlank
    @Size(max = 120)
    private String reference;

    @Valid
    @NotEmpty
    private List<OrderLineForm> lines = new ArrayList<>(List.of(new OrderLineForm()));

    public CreateOrderRequest toRequest() {
        return new CreateOrderRequest(reference, lines.stream().map(OrderLineForm::toRequest).toList());
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<OrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineForm> lines) {
        this.lines = lines;
    }
}
