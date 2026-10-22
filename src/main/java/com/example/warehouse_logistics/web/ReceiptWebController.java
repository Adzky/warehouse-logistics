package com.example.warehouse_logistics.web;

import com.example.warehouse_logistics.dto.ReceiptResponse;
import com.example.warehouse_logistics.entity.ReceiptStatus;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.ReceivingService;
import com.example.warehouse_logistics.web.form.ReceiptForm;
import com.example.warehouse_logistics.web.form.ReceiveForm;
import com.example.warehouse_logistics.web.form.ReceiveLineForm;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class ReceiptWebController {

    private final ReceivingService receivingService;
    private final CatalogService catalogService;

    public ReceiptWebController(ReceivingService receivingService, CatalogService catalogService) {
        this.receivingService = receivingService;
        this.catalogService = catalogService;
    }

    @GetMapping("/receipts")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) ReceiptStatus status,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       Model model) {
        String query = q == null ? "" : q.trim();
        List<ReceiptResponse> filtered = receivingService.findAll().stream()
                .filter(receipt -> status == null || receipt.status() == status)
                .filter(receipt -> matches(receipt, query))
                .toList();
        populateCreateModel(model);
        model.addAttribute("currentPage", "receipts");
        model.addAttribute("q", query);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ReceiptStatus.values());
        model.addAttribute("page", PageView.of(filtered, page, size));
        return "receipts/list";
    }

    @PostMapping("/receipts")
    public String create(@Valid @ModelAttribute("receiptForm") ReceiptForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return list(null, null, 1, 20, model);
        }
        ReceiptResponse created = receivingService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Inbound receipt created.");
        return "redirect:/receipts/" + created.id();
    }

    @GetMapping("/receipts/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        ReceiptResponse receipt = receivingService.findById(id);
        ReceiveForm receiveForm = new ReceiveForm();
        receiveForm.setLines(new ArrayList<>(receipt.lines().stream().map(line -> {
            ReceiveLineForm lineForm = new ReceiveLineForm();
            lineForm.setLineId(line.id());
            lineForm.setQuantity(0L);
            return lineForm;
        }).toList()));
        model.addAttribute("currentPage", "receipts");
        model.addAttribute("receipt", receipt);
        if (!model.containsAttribute("receiveForm")) {
            model.addAttribute("receiveForm", receiveForm);
        }
        return "receipts/detail";
    }

    @PostMapping("/receipts/{id}/receive")
    public String receive(@PathVariable UUID id,
                          @ModelAttribute("receiveForm") ReceiveForm form,
                          RedirectAttributes redirectAttributes) {
        var request = form.toRequest();
        if (request.lines() == null || request.lines().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Enter a quantity greater than zero for at least one line.");
            return "redirect:/receipts/" + id;
        }
        receivingService.receive(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Quantities received and put away.");
        return "redirect:/receipts/" + id;
    }

    private void populateCreateModel(Model model) {
        model.addAttribute("products", catalogService.findProducts());
        model.addAttribute("locations", catalogService.findLocations(null));
        model.addAttribute("suppliers", catalogService.findSuppliers());
        if (!model.containsAttribute("receiptForm")) {
            model.addAttribute("receiptForm", new ReceiptForm());
        }
    }

    private boolean matches(ReceiptResponse receipt, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (receipt.reference() + " "
                + (receipt.supplierName() == null ? "" : receipt.supplierName())
                + " " + receipt.status()).toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }
}
