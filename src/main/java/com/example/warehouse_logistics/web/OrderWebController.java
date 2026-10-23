package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.OrderResponse;
import com.example.warehouse_logistics.dto.LocationResponse;
import com.example.warehouse_logistics.dto.ProductResponse;
import com.example.warehouse_logistics.entity.OutboundOrderStatus;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.OutboundOrderService;
import com.example.warehouse_logistics.web.form.OrderForm;
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
public class OrderWebController {

    private final OutboundOrderService orderService;
    private final CatalogService catalogService;

    public OrderWebController(OutboundOrderService orderService, CatalogService catalogService) {
        this.orderService = orderService;
        this.catalogService = catalogService;
    }

    @GetMapping("/orders")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) OutboundOrderStatus status,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<OrderResponse> filtered = orderService.findAll().stream()
                .filter(order -> status == null || order.status() == status)
                .filter(order -> matches(order, query))
                .toList();
        populateCreateModel(model);
        model.addAttribute("currentPage", "orders");
        model.addAttribute("q", query);
        model.addAttribute("status", status);
        model.addAttribute("statuses", OutboundOrderStatus.values());
        model.addAttribute("page", PageView.of(filtered, page, size));
        return "orders/list";
    }

    @PostMapping("/orders")
    public String create(@Valid @ModelAttribute("orderForm") OrderForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, 1, 20, model);
        }
        OrderResponse created = orderService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Order created and stock reserved.");
        return "redirect:/orders/" + created.id();
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("currentPage", "orders");
        model.addAttribute("order", orderService.findById(id));
        return "orders/detail";
    }

    @PostMapping("/orders/{id}/pick")
    public String pick(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        orderService.pick(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as picked.");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/pack")
    public String pack(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        orderService.pack(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as packed.");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/ship")
    public String ship(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        orderService.ship(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order shipped and stock deducted.");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        orderService.cancel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order cancelled and reservation released.");
        return "redirect:/orders/" + id;
    }

    private void populateCreateModel(Model model) {
        model.addAttribute("products", catalogService.findProducts().stream()
                .filter(ProductResponse::active).toList());
        model.addAttribute("locations", catalogService.findLocations(null).stream()
                .filter(LocationResponse::active).toList());
        if (!model.containsAttribute("orderForm")) {
            model.addAttribute("orderForm", new OrderForm());
        }
    }

    private boolean matches(OrderResponse order, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (order.reference() + " " + order.status()).toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
