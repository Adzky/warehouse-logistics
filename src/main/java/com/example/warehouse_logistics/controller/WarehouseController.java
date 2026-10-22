package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.WarehouseRequest;
import com.example.warehouse_logistics.dto.WarehouseResponse;
import com.example.warehouse_logistics.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public WarehouseResponse create(@Valid @RequestBody WarehouseRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<WarehouseResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public WarehouseResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}