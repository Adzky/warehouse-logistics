package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.InventoryResponse;
import com.example.warehouse_logistics.dto.OrderResponse;
import com.example.warehouse_logistics.dto.ReceiptResponse;
import com.example.warehouse_logistics.entity.OutboundOrderStatus;
import com.example.warehouse_logistics.entity.ReceiptStatus;
import com.example.warehouse_logistics.service.InventoryService;
import com.example.warehouse_logistics.service.OutboundOrderService;
import com.example.warehouse_logistics.service.ReceivingService;
import com.example.warehouse_logistics.service.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.EnumSet;
import java.util.List;

@Controller
public class DashboardController {

    private static final EnumSet<OutboundOrderStatus> OPEN_ORDERS = EnumSet.of(
            OutboundOrderStatus.RESERVED, OutboundOrderStatus.PICKED, OutboundOrderStatus.PACKED);

    private final InventoryService inventoryService;
    private final ReceivingService receivingService;
    private final OutboundOrderService orderService;
    private final WarehouseService warehouseService;

    public DashboardController(InventoryService inventoryService, ReceivingService receivingService,
                               OutboundOrderService orderService, WarehouseService warehouseService) {
        this.inventoryService = inventoryService;
        this.receivingService = receivingService;
        this.orderService = orderService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<InventoryResponse> alerts = inventoryService.getInventory().stream()
                .filter(InventoryResponse::belowReorderPoint)
                .toList();
        List<ReceiptResponse> inbound = receivingService.findAll().stream()
                .filter(receipt -> receipt.status() != ReceiptStatus.RECEIVED)
                .toList();
        List<OrderResponse> awaiting = orderService.findAll().stream()
                .filter(order -> OPEN_ORDERS.contains(order.status()))
                .toList();

        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("warehouseCount", warehouseService.findAll().size());
        model.addAttribute("alertCount", alerts.size());
        model.addAttribute("inboundCount", inbound.size());
        model.addAttribute("openOrderCount", awaiting.size());
        model.addAttribute("alerts", alerts.stream().limit(8).toList());
        model.addAttribute("inbound", inbound.stream().limit(8).toList());
        model.addAttribute("orders", awaiting.stream().limit(8).toList());
        return "dashboard";
    }
}
