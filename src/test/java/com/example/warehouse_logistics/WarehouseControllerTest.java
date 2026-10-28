package com.example.warehouse_logistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createWarehouseReturnsCreatedResource() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"code":"WH-TEST-01","name":"Test Warehouse","location":"Chicago, IL"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("WH-TEST-01"))
                .andExpect(jsonPath("$.name").value("Test Warehouse"));
    }

    @Test
    void createWarehouseRejectsInvalidFields() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"code":"","name":" ","location":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createWarehouseRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body is missing or invalid"));
    }

    @Test
    void createWarehouseRejectsDuplicateCode() throws Exception {
        String body = """
                {"code":"WH-DUPLICATE","name":"First","location":"A"}
                """;

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listWarehousesReturnsSuccessfulResponse() throws Exception {
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findMissingWarehouseReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/warehouses/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}