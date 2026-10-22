package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.LocationRequest;
import com.example.warehouse_logistics.dto.LocationResponse;
import com.example.warehouse_logistics.dto.ProductRequest;
import com.example.warehouse_logistics.dto.ProductResponse;
import com.example.warehouse_logistics.dto.SupplierRequest;
import com.example.warehouse_logistics.dto.SupplierResponse;
import com.example.warehouse_logistics.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return service.createProduct(request);
    }

    @GetMapping("/products")
    public List<ProductResponse> findProducts() {
        return service.findProducts();
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse createLocation(@Valid @RequestBody LocationRequest request) {
        return service.createLocation(request);
    }

    @GetMapping("/locations")
    public List<LocationResponse> findLocations(
            @RequestParam(required = false) UUID warehouseId) {
        return service.findLocations(warehouseId);
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        return service.createSupplier(request);
    }

    @GetMapping("/suppliers")
    public List<SupplierResponse> findSuppliers() {
        return service.findSuppliers();
    }
}
