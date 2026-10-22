package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.CreateReceiptRequest;
import com.example.warehouse_logistics.dto.ReceiveRequest;
import com.example.warehouse_logistics.dto.ReceiptResponse;
import com.example.warehouse_logistics.service.ReceivingService;
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
@RequestMapping("/api/receipts")
public class ReceivingController {

    private final ReceivingService service;

    public ReceivingController(ReceivingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptResponse create(@Valid @RequestBody CreateReceiptRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ReceiptResponse> findAll() {
        return service.findAll();
    }

    @PostMapping("/{id}/receive")
    public ReceiptResponse receive(@PathVariable UUID id, @Valid @RequestBody ReceiveRequest request) {
        return service.receive(id, request);
    }
}
