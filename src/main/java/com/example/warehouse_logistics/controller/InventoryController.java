package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.InventoryResponse;
import com.example.warehouse_logistics.dto.ReorderPointRequest;
import com.example.warehouse_logistics.dto.StockAdjustmentRequest;
import com.example.warehouse_logistics.dto.StockMoveRequest;
import com.example.warehouse_logistics.dto.StockMovementResponse;
import com.example.warehouse_logistics.dto.StockReceiptRequest;
import com.example.warehouse_logistics.service.InventoryService;
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
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventoryResponse> getInventory() {
        return service.getInventory();
    }

    @GetMapping("/movements")
    public List<StockMovementResponse> getMovements(
            @RequestParam(required = false) UUID productId) {
        return service.getMovements(productId);
    }

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse receive(@Valid @RequestBody StockReceiptRequest request) {
        return service.receive(request);
    }

    @PostMapping("/move")
    public List<InventoryResponse> move(@Valid @RequestBody StockMoveRequest request) {
        return service.move(request);
    }

    @PostMapping("/adjust")
    public InventoryResponse adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return service.adjust(request);
    }

    @PostMapping("/reorder-point")
    public InventoryResponse setReorderPoint(@Valid @RequestBody ReorderPointRequest request) {
        return service.setReorderPoint(request);
    }
}
