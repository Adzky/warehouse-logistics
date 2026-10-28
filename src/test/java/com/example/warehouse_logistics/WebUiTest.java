package com.example.warehouse_logistics;

import com.example.warehouse_logistics.dto.LocationRequest;
import com.example.warehouse_logistics.dto.ProductRequest;
import com.example.warehouse_logistics.dto.SupplierRequest;
import com.example.warehouse_logistics.dto.WarehouseRequest;
import com.example.warehouse_logistics.entity.LocationType;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebUiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private WarehouseService warehouseService;

    @Test
    void dashboardRendersOperationalSummary() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Warehouse dashboard")))
                .andExpect(content().string(containsString("Inventory alerts")));
    }

    @Test
    void warehouseFormRejectsBlankFields() throws Exception {
        mockMvc.perform(post("/warehouses")
                        .param("code", "")
                        .param("name", " ")
                        .param("location", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("warehouses/list"))
                .andExpect(model().attributeHasFieldErrors("warehouseForm", "code", "name", "location"));
    }

    @Test
    void warehouseFormCreatesRecordAndRedirects() throws Exception {
        mockMvc.perform(post("/warehouses")
                        .param("code", "WH-UI-01")
                        .param("name", "UI Warehouse")
                        .param("location", "Austin, TX"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/warehouses"));

        mockMvc.perform(get("/warehouses").param("q", "WH-UI-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WH-UI-01")))
                .andExpect(content().string(containsString("UI Warehouse")))
                .andExpect(content().string(containsString(">Address</label>")))
                .andExpect(content().string(containsString("<th>Address</th>")));
    }

    @Test
    void newlyCreatedWarehouseHasSelectableDefaultLocation() throws Exception {
        var warehouse = warehouseService.create(new WarehouseRequest(
                "WH-UI-LOCATION", "Location Warehouse", "Austin, TX"));

        var locations = catalogService.findLocations(warehouse.id());
        org.junit.jupiter.api.Assertions.assertEquals(1, locations.size());
        org.junit.jupiter.api.Assertions.assertEquals("MAIN", locations.getFirst().code());
        org.junit.jupiter.api.Assertions.assertEquals(LocationType.STORAGE, locations.getFirst().type());

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WH-UI-LOCATION / MAIN")));
    }

    @Test
    void productSearchAndInventoryPagesRender() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));
        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/list"));
        mockMvc.perform(get("/receipts"))
                .andExpect(status().isOk())
                .andExpect(view().name("receipts/list"));
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"));
        mockMvc.perform(get("/movements"))
                .andExpect(status().isOk())
                .andExpect(view().name("movements/list"));
        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isOk())
                .andExpect(view().name("suppliers/list"));
    }

    @Test
    void missingWarehousePageReturnsNotFoundView() throws Exception {
        mockMvc.perform(get("/warehouses/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/not-found"));
    }

    @Test
    void productDetailCanBeEditedArchivedAndRestored() throws Exception {
        var product = catalogService.createProduct(
                new ProductRequest("UI-PRODUCT-01", "Original", null, "each"));

        mockMvc.perform(get("/products/{id}", product.id()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"));
        mockMvc.perform(post("/products/{id}", product.id())
                        .param("sku", "UI-PRODUCT-01")
                        .param("name", "Updated")
                        .param("description", "")
                        .param("unitOfMeasure", "case"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/products/" + product.id()));
        mockMvc.perform(post("/products/{id}/archive", product.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertFalse(catalogService.findProduct(product.id()).active());
        mockMvc.perform(post("/products/{id}/restore", product.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertTrue(catalogService.findProduct(product.id()).active());
    }

    @Test
    void supplierDetailCanBeEditedArchivedAndRestored() throws Exception {
        var supplier = catalogService.createSupplier(
                new SupplierRequest("UI-SUPPLIER-01", "Original", "supplier@example.com"));

        mockMvc.perform(get("/suppliers/{id}", supplier.id()))
                .andExpect(status().isOk())
                .andExpect(view().name("suppliers/detail"));
        mockMvc.perform(post("/suppliers/{id}", supplier.id())
                        .param("code", "UI-SUPPLIER-01")
                        .param("name", "Updated")
                        .param("contactEmail", "updated@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/suppliers/" + supplier.id()));
        mockMvc.perform(post("/suppliers/{id}/archive", supplier.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertFalse(catalogService.findSupplier(supplier.id()).active());
        mockMvc.perform(post("/suppliers/{id}/restore", supplier.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertTrue(catalogService.findSupplier(supplier.id()).active());
    }

    @Test
    void locationDetailCanBeEditedArchivedAndRestored() throws Exception {
        var warehouse = warehouseService.create(new WarehouseRequest(
                "WH-UI-LOC-01", "Location UI Warehouse", "Austin"));
        var location = catalogService.createLocation(new LocationRequest(
                warehouse.id(), null, "UI-LOC-01", "Original", LocationType.STORAGE));

        mockMvc.perform(get("/warehouses/{id}/locations/{locationId}", warehouse.id(), location.id()))
                .andExpect(status().isOk())
                .andExpect(view().name("warehouses/location-detail"));
        mockMvc.perform(post("/warehouses/{id}/locations/{locationId}", warehouse.id(), location.id())
                        .param("code", "UI-LOC-01")
                        .param("name", "Updated"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/warehouses/" + warehouse.id() + "/locations/" + location.id()));
        mockMvc.perform(post("/warehouses/{id}/locations/{locationId}/archive", warehouse.id(), location.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertFalse(catalogService.findLocation(location.id()).active());
        mockMvc.perform(post("/warehouses/{id}/locations/{locationId}/restore", warehouse.id(), location.id()))
                .andExpect(status().isFound());
        org.junit.jupiter.api.Assertions.assertTrue(catalogService.findLocation(location.id()).active());
    }

    @Test
    void locationWithChildCannotBeArchived() throws Exception {
        var warehouse = warehouseService.create(new WarehouseRequest(
                "WH-UI-CHILD-01", "Location UI Warehouse", "Austin"));
        var zone = catalogService.createLocation(new LocationRequest(
                warehouse.id(), null, "UI-ZONE-01", "Zone", LocationType.ZONE));
        catalogService.createLocation(new LocationRequest(
                warehouse.id(), zone.id(), "UI-BIN-01", "Bin", LocationType.BIN));

        mockMvc.perform(post("/warehouses/{id}/locations/{locationId}/archive", warehouse.id(), zone.id())
                        .header("Referer", "/warehouses/" + warehouse.id() + "/locations/" + zone.id()))
                .andExpect(status().isFound());

        org.junit.jupiter.api.Assertions.assertTrue(catalogService.findLocation(zone.id()).active());
    }

    @Test
    void apiCreateStillReturnsJson() throws Exception {
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/json")));
    }
}
