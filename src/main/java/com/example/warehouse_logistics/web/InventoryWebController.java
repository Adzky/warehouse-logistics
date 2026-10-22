package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.InventoryResponse;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.InventoryService;
import com.example.warehouse_logistics.service.WarehouseService;
import com.example.warehouse_logistics.web.form.AdjustStockForm;
import com.example.warehouse_logistics.web.form.DirectReceiveForm;
import com.example.warehouse_logistics.web.form.MoveStockForm;
import com.example.warehouse_logistics.web.form.ReorderPointForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class InventoryWebController {

    private final InventoryService inventoryService;
    private final CatalogService catalogService;
    private final WarehouseService warehouseService;

    public InventoryWebController(InventoryService inventoryService, CatalogService catalogService,
                                  WarehouseService warehouseService) {
        this.inventoryService = inventoryService;
        this.catalogService = catalogService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/inventory")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) UUID warehouseId,
                       @RequestParam(required = false) UUID locationId,
                       @RequestParam(required = false) Boolean alertsOnly,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        boolean onlyAlerts = Boolean.TRUE.equals(alertsOnly);
        List<InventoryResponse> filtered = inventoryService.getInventory().stream()
                .filter(item -> warehouseId == null || warehouseId.equals(item.warehouseId()))
                .filter(item -> locationId == null || locationId.equals(item.locationId()))
                .filter(item -> !onlyAlerts || item.belowReorderPoint())
                .filter(item -> matches(item, query))
                .toList();
        populateLookups(model);
        model.addAttribute("currentPage", "inventory");
        model.addAttribute("q", query);
        model.addAttribute("warehouseId", warehouseId);
        model.addAttribute("locationId", locationId);
        model.addAttribute("alertsOnly", onlyAlerts);
        model.addAttribute("page", PageView.of(filtered, page, size));
        return "inventory/list";
    }

    @PostMapping("/inventory/receive")
    public String receive(@Valid @ModelAttribute("directReceiveForm") DirectReceiveForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, null, false, 1, 20, model);
        }
        inventoryService.receive(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Stock received into the selected location.");
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/move")
    public String move(@Valid @ModelAttribute("moveStockForm") MoveStockForm form,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, null, false, 1, 20, model);
        }
        inventoryService.move(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Stock moved between locations.");
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/adjust")
    public String adjust(@Valid @ModelAttribute("adjustStockForm") AdjustStockForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, null, false, 1, 20, model);
        }
        inventoryService.adjust(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Stock adjustment applied.");
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/reorder-point")
    public String reorderPoint(@Valid @ModelAttribute("reorderPointForm") ReorderPointForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, null, false, 1, 20, model);
        }
        inventoryService.setReorderPoint(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Reorder point updated.");
        return "redirect:/inventory";
    }

    private void populateLookups(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        model.addAttribute("products", catalogService.findProducts());
        model.addAttribute("locations", catalogService.findLocations(null));
        if (!model.containsAttribute("directReceiveForm")) {
            model.addAttribute("directReceiveForm", new DirectReceiveForm());
        }
        if (!model.containsAttribute("moveStockForm")) {
            model.addAttribute("moveStockForm", new MoveStockForm());
        }
        if (!model.containsAttribute("adjustStockForm")) {
            model.addAttribute("adjustStockForm", new AdjustStockForm());
        }
        if (!model.containsAttribute("reorderPointForm")) {
            model.addAttribute("reorderPointForm", new ReorderPointForm());
        }
    }

    private boolean matches(InventoryResponse item, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (item.sku() + " " + item.productName() + " " + item.warehouseCode()
                + " " + item.locationCode()).toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
