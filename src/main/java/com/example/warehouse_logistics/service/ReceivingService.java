package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.CreateReceiptRequest;
import com.example.warehouse_logistics.dto.ReceiveRequest;
import com.example.warehouse_logistics.dto.ReceiptResponse;
import com.example.warehouse_logistics.entity.InboundReceipt;
import com.example.warehouse_logistics.entity.InboundReceiptLine;
import com.example.warehouse_logistics.entity.Product;
import com.example.warehouse_logistics.entity.Supplier;
import com.example.warehouse_logistics.entity.WarehouseLocation;
import com.example.warehouse_logistics.exception.DuplicateResourceException;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.repository.InboundReceiptRepository;
import com.example.warehouse_logistics.repository.ProductRepository;
import com.example.warehouse_logistics.repository.SupplierRepository;
import com.example.warehouse_logistics.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Comparator;

@Service
public class ReceivingService {

    private final InboundReceiptRepository receipts;
    private final ProductRepository products;
    private final SupplierRepository suppliers;
    private final WarehouseLocationRepository locations;
    private final InventoryService inventory;

    public ReceivingService(InboundReceiptRepository receipts, ProductRepository products,
                            SupplierRepository suppliers,
                            WarehouseLocationRepository locations, InventoryService inventory) {
        this.receipts = receipts;
        this.products = products;
        this.suppliers = suppliers;
        this.locations = locations;
        this.inventory = inventory;
    }

    @Transactional
    public ReceiptResponse create(CreateReceiptRequest request) {
        if (receipts.existsByReference(request.reference())) {
            throw new DuplicateResourceException("Receipt reference already exists: " + request.reference());
        }
        Supplier supplier = request.supplierId() == null ? null
                : suppliers.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        InboundReceipt receipt = new InboundReceipt(request.reference(), supplier);
        for (var requestLine : request.lines().stream()
                .sorted(Comparator.comparing(line -> line.productId().toString()))
                .toList()) {
            Product product = products.findById(requestLine.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", requestLine.productId()));
            WarehouseLocation location = locations.findById(requestLine.destinationLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Location", requestLine.destinationLocationId()));
            receipt.addLine(new InboundReceiptLine(
                    receipt, product, location, requestLine.expectedQuantity()));
        }
        return ReceiptResponse.from(receipts.save(receipt));
    }

    @Transactional(readOnly = true)
    public java.util.List<ReceiptResponse> findAll() {
        return receipts.findAllByOrderByCreatedAtDesc().stream()
                .map(ReceiptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReceiptResponse findById(UUID id) {
        return ReceiptResponse.from(receipts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", id)));
    }

    @Transactional
    public ReceiptResponse receive(UUID id, ReceiveRequest request) {
        InboundReceipt receipt = receipts.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", id));
        for (var requestedLine : request.lines()) {
            InboundReceiptLine line = receipt.getLines().stream()
                    .filter(candidate -> candidate.getId().equals(requestedLine.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Receipt line does not belong to this receipt"));
            line.receive(requestedLine.quantity());
            inventory.receive(new com.example.warehouse_logistics.dto.StockReceiptRequest(
                    line.getProduct().getId(),
                    line.getDestinationLocation().getId(),
                    requestedLine.quantity(),
                    receipt.getReference()
            ));
        }
        receipt.updateStatus();
        return ReceiptResponse.from(receipt);
    }
}
