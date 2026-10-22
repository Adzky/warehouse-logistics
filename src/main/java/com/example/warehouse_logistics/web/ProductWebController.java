package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.ProductResponse;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.web.form.ProductForm;
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
public class ProductWebController {

    private final CatalogService catalogService;

    public ProductWebController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<ProductResponse> filtered = catalogService.findProducts().stream()
                .filter(product -> matches(product, query))
                .toList();
        model.addAttribute("currentPage", "products");
        model.addAttribute("q", query);
        model.addAttribute("page", PageView.of(filtered, page, size));
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        return "products/list";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, 1, 20, model);
        }
        catalogService.createProduct(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Product created.");
        return "redirect:/products";
    }

    private boolean matches(ProductResponse product, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (product.sku() + " " + product.name() + " "
                + (product.description() == null ? "" : product.description()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
