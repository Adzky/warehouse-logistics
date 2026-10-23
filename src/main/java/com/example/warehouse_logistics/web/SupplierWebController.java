package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.SupplierResponse;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.web.form.SupplierForm;
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
public class SupplierWebController {

    private final CatalogService catalogService;

    public SupplierWebController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/suppliers")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<SupplierResponse> filtered = catalogService.findSuppliers().stream()
                .filter(supplier -> matches(supplier, query))
                .toList();
        model.addAttribute("currentPage", "suppliers");
        model.addAttribute("q", query);
        model.addAttribute("page", PageView.of(filtered, page, size));
        if (!model.containsAttribute("supplierForm")) {
            model.addAttribute("supplierForm", new SupplierForm());
        }
        return "suppliers/list";
    }

    @PostMapping("/suppliers")
    public String create(@Valid @ModelAttribute("supplierForm") SupplierForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, 1, 20, model);
        }
        catalogService.createSupplier(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier created.");
        return "redirect:/suppliers";
    }

    @GetMapping("/suppliers/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        var supplier = catalogService.findSupplier(id);
        model.addAttribute("currentPage", "suppliers");
        model.addAttribute("supplier", supplier);
        if (!model.containsAttribute("supplierForm")) {
            SupplierForm form = new SupplierForm();
            form.setCode(supplier.code());
            form.setName(supplier.name());
            form.setContactEmail(supplier.contactEmail());
            model.addAttribute("supplierForm", form);
        }
        return "suppliers/detail";
    }

    @PostMapping("/suppliers/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("supplierForm") SupplierForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return detail(id, model);
        }
        catalogService.updateSupplier(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier updated.");
        return "redirect:/suppliers/" + id;
    }

    @PostMapping("/suppliers/{id}/archive")
    public String archive(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        catalogService.setSupplierActive(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier archived.");
        return "redirect:/suppliers/" + id;
    }

    @PostMapping("/suppliers/{id}/restore")
    public String restore(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        catalogService.setSupplierActive(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier restored.");
        return "redirect:/suppliers/" + id;
    }

    private boolean matches(SupplierResponse supplier, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (supplier.code() + " " + supplier.name() + " "
                + (supplier.contactEmail() == null ? "" : supplier.contactEmail()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
