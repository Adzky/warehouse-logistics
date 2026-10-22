package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.CreateOrderRequest;
import com.example.warehouse_logistics.dto.OrderResponse;
import com.example.warehouse_logistics.service.OutboundOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OutboundOrderController {

    private final OutboundOrderService service;

    public OutboundOrderController(OutboundOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<OrderResponse> findAll() {
        return service.findAll();
    }

    @PostMapping("/{id}/pick")
    public OrderResponse pick(@PathVariable UUID id) {
        return service.pick(id);
    }

    @PostMapping("/{id}/pack")
    public OrderResponse pack(@PathVariable UUID id) {
        return service.pack(id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable UUID id) {
        return service.ship(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}
