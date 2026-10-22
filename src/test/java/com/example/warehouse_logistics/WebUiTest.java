package com.example.warehouse_logistics;

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
                .andExpect(content().string(containsString("UI Warehouse")));
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
    void apiCreateStillReturnsJson() throws Exception {
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/json")));
    }
}
