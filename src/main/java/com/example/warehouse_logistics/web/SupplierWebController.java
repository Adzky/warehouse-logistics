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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

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
