package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.LocationResponse;
import com.example.warehouse_logistics.dto.WarehouseResponse;
import com.example.warehouse_logistics.entity.LocationType;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.WarehouseService;
import com.example.warehouse_logistics.web.form.LocationForm;
import com.example.warehouse_logistics.web.form.LocationEditForm;
import com.example.warehouse_logistics.web.form.WarehouseForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class WarehouseWebController {

    private final WarehouseService warehouseService;
    private final CatalogService catalogService;

    public WarehouseWebController(WarehouseService warehouseService, CatalogService catalogService) {
        this.warehouseService = warehouseService;
        this.catalogService = catalogService;
    }

    @GetMapping("/warehouses")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<WarehouseResponse> filtered = warehouseService.findAll().stream()
                .filter(warehouse -> matches(warehouse, query))
                .toList();
        model.addAttribute("currentPage", "warehouses");
        model.addAttribute("q", query);
        model.addAttribute("page", PageView.of(filtered, page, size));
        if (!model.containsAttribute("warehouseForm")) {
            model.addAttribute("warehouseForm", new WarehouseForm());
        }
        return "warehouses/list";
    }

    @PostMapping("/warehouses")
    public String create(@Valid @ModelAttribute("warehouseForm") WarehouseForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, 1, 20, model);
        }
        warehouseService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Warehouse created.");
        return "redirect:/warehouses";
    }

    @GetMapping("/warehouses/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        WarehouseResponse warehouse = warehouseService.findById(id);
        List<LocationResponse> locations = catalogService.findLocations(id);
        model.addAttribute("currentPage", "warehouses");
        model.addAttribute("warehouse", warehouse);
        model.addAttribute("locations", locations);
        model.addAttribute("locationTypes", LocationType.values());
        if (!model.containsAttribute("locationForm")) {
            LocationForm form = new LocationForm();
            form.setWarehouseId(id);
            model.addAttribute("locationForm", form);
        }
        return "warehouses/detail";
    }

    @PostMapping("/warehouses/{id}/locations")
    public String createLocation(@PathVariable UUID id,
                                 @Valid @ModelAttribute("locationForm") LocationForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        form.setWarehouseId(id);
        if (bindingResult.hasErrors()) {
            return detail(id, model);
        }
        catalogService.createLocation(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Location created.");
        return "redirect:/warehouses/" + id;
    }

    @GetMapping("/warehouses/{id}/locations/{locationId}")
    public String locationDetail(@PathVariable UUID id, @PathVariable UUID locationId, Model model) {
        WarehouseResponse warehouse = warehouseService.findById(id);
        LocationResponse location = catalogService.findLocation(locationId);
        if (!location.warehouseId().equals(warehouse.id())) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        model.addAttribute("currentPage", "warehouses");
        model.addAttribute("warehouse", warehouse);
        model.addAttribute("location", location);
        if (!model.containsAttribute("locationEditForm")) {
            LocationEditForm form = new LocationEditForm();
            form.setCode(location.code());
            form.setName(location.name());
            model.addAttribute("locationEditForm", form);
        }
        return "warehouses/location-detail";
    }

    @PostMapping("/warehouses/{id}/locations/{locationId}")
    public String updateLocation(@PathVariable UUID id, @PathVariable UUID locationId,
                                 @Valid @ModelAttribute("locationEditForm") LocationEditForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return locationDetail(id, locationId, model);
        }
        catalogService.updateLocation(locationId, form.getCode(), form.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Location updated.");
        return "redirect:/warehouses/" + id + "/locations/" + locationId;
    }

    @PostMapping("/warehouses/{id}/locations/{locationId}/archive")
    public String archiveLocation(@PathVariable UUID id, @PathVariable UUID locationId,
                                  RedirectAttributes redirectAttributes) {
        ensureLocationBelongsToWarehouse(id, locationId);
        catalogService.setLocationActive(locationId, false);
        redirectAttributes.addFlashAttribute("successMessage", "Location archived.");
        return "redirect:/warehouses/" + id + "/locations/" + locationId;
    }

    @PostMapping("/warehouses/{id}/locations/{locationId}/restore")
    public String restoreLocation(@PathVariable UUID id, @PathVariable UUID locationId,
                                  RedirectAttributes redirectAttributes) {
        ensureLocationBelongsToWarehouse(id, locationId);
        catalogService.setLocationActive(locationId, true);
        redirectAttributes.addFlashAttribute("successMessage", "Location restored.");
        return "redirect:/warehouses/" + id + "/locations/" + locationId;
    }

    @PostMapping("/warehouses/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        warehouseService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Warehouse deleted.");
        return "redirect:/warehouses";
    }

    private boolean matches(WarehouseResponse warehouse, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (warehouse.code() + " " + warehouse.name() + " " + warehouse.location())
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }

    private void ensureLocationBelongsToWarehouse(UUID warehouseId, UUID locationId) {
        WarehouseResponse warehouse = warehouseService.findById(warehouseId);
        LocationResponse location = catalogService.findLocation(locationId);
        if (!location.warehouseId().equals(warehouse.id())) {
            throw new ResourceNotFoundException("Location", locationId);
        }
    }
}
