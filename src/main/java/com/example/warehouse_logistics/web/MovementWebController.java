package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.StockMovementResponse;
import com.example.warehouse_logistics.entity.MovementType;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class MovementWebController {

    private final InventoryService inventoryService;
    private final CatalogService catalogService;

    public MovementWebController(InventoryService inventoryService, CatalogService catalogService) {
        this.inventoryService = inventoryService;
        this.catalogService = catalogService;
    }

    @GetMapping("/movements")
    public String list(@RequestParam(required = false) UUID productId,
                       @RequestParam(required = false) MovementType type,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<StockMovementResponse> filtered = inventoryService.getMovements(productId).stream()
                .filter(movement -> type == null || movement.type() == type)
                .filter(movement -> matches(movement, query))
                .toList();
        model.addAttribute("currentPage", "movements");
        model.addAttribute("products", catalogService.findProducts());
        model.addAttribute("types", MovementType.values());
        model.addAttribute("productId", productId);
        model.addAttribute("type", type);
        model.addAttribute("q", query);
        model.addAttribute("page", PageView.of(filtered, page, size));
        model.addAttribute("receiveCount", count(filtered, MovementType.RECEIVE));
        model.addAttribute("moveCount", count(filtered, MovementType.MOVE));
        model.addAttribute("shipCount", count(filtered, MovementType.SHIP));
        model.addAttribute("adjustCount", count(filtered, MovementType.ADJUST_IN, MovementType.ADJUST_OUT));
        return "movements/list";
    }

    private long count(List<StockMovementResponse> movements, MovementType... types) {
        List<MovementType> wanted = Arrays.asList(types);
        return movements.stream().filter(movement -> wanted.contains(movement.type())).count();
    }

    private boolean matches(StockMovementResponse movement, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (movement.sku() + " " + movement.type() + " "
                + (movement.reference() == null ? "" : movement.reference()) + " "
                + (movement.sourceLocationCode() == null ? "" : movement.sourceLocationCode()) + " "
                + (movement.destinationLocationCode() == null ? "" : movement.destinationLocationCode()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
