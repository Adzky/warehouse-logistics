package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.CreateOrderRequest;
import com.example.warehouse_logistics.dto.OrderResponse;
import com.example.warehouse_logistics.entity.OutboundOrder;
import com.example.warehouse_logistics.entity.OutboundOrderLine;
import com.example.warehouse_logistics.entity.Product;
import com.example.warehouse_logistics.entity.WarehouseLocation;
import com.example.warehouse_logistics.exception.DuplicateResourceException;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.repository.OutboundOrderRepository;
import com.example.warehouse_logistics.repository.ProductRepository;
import com.example.warehouse_logistics.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;

@Service
public class OutboundOrderService {

    private final OutboundOrderRepository orders;
    private final ProductRepository products;
    private final WarehouseLocationRepository locations;
    private final InventoryService inventory;

    public OutboundOrderService(OutboundOrderRepository orders, ProductRepository products,
                                WarehouseLocationRepository locations, InventoryService inventory) {
        this.orders = orders;
        this.products = products;
        this.locations = locations;
        this.inventory = inventory;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (orders.existsByReference(request.reference())) {
            throw new DuplicateResourceException("Order reference already exists: " + request.reference());
        }
        OutboundOrder order = new OutboundOrder(request.reference());
        var lines = request.lines().stream()
                .sorted(Comparator.comparing(line -> line.productId().toString()))
                .toList();
        for (var requestedLine : lines) {
            Product product = products.findById(requestedLine.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", requestedLine.productId()));
            WarehouseLocation location = locations.findById(requestedLine.sourceLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Location", requestedLine.sourceLocationId()));
            inventory.reserve(product.getId(), location.getId(),
                    requestedLine.quantity(), request.reference());
            order.addLine(new OutboundOrderLine(
                    order, product, location, requestedLine.quantity()));
        }
        return OrderResponse.from(orders.save(order));
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderResponse> findAll() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return OrderResponse.from(orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id)));
    }

    @Transactional
    public OrderResponse pick(UUID id) {
        OutboundOrder order = loadOrder(id);
        order.markPicked();
        order.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId().toString()))
                .forEach(line -> inventory.pick(
                        line.getProduct().getId(),
                        line.getSourceLocation().getId(),
                        line.getQuantity(),
                        order.getReference()));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse pack(UUID id) {
        OutboundOrder order = loadOrder(id);
        order.markPacked();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse ship(UUID id) {
        OutboundOrder order = loadOrder(id);
        order.markShipped();
        order.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId().toString()))
                .forEach(line -> inventory.ship(
                        line.getProduct().getId(),
                        line.getSourceLocation().getId(),
                        line.getQuantity(),
                        order.getReference()));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        OutboundOrder order = loadOrder(id);
        order.cancel();
        order.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId().toString()))
                .forEach(line -> inventory.release(
                        line.getProduct().getId(),
                        line.getSourceLocation().getId(),
                        line.getQuantity(),
                        order.getReference()));
        return OrderResponse.from(order);
    }

    private OutboundOrder loadOrder(UUID id) {
        return orders.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
