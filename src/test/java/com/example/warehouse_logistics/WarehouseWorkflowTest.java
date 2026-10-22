package com.example.warehouse_logistics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WarehouseWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void receivesPartialStockAndFulfillsReservedOrder() throws Exception {
        String warehouse = createResource("/api/warehouses", """
                {"code":"WH-FLOW","name":"Flow Warehouse","location":"Test"}
                """);
        UUID warehouseId = uuid(warehouse, "id");

        String product = createResource("/api/products", """
                {"sku":"SKU-FLOW","name":"Workflow Product","unitOfMeasure":"each"}
                """);
        UUID productId = uuid(product, "id");

        String supplier = createResource("/api/suppliers", """
                {"code":"SUP-FLOW","name":"Flow Supplier","contactEmail":"ops@example.test"}
                """);
        UUID supplierId = uuid(supplier, "id");

        String zone = createResource("/api/locations", """
                {"warehouseId":"%s","code":"ZONE-A","name":"Zone A","type":"ZONE"}
                """.formatted(warehouseId));
        UUID zoneId = uuid(zone, "id");

        String location = createResource("/api/locations", """
                {"warehouseId":"%s","parentLocationId":"%s","code":"A-01",
                 "name":"Storage A-01","type":"BIN"}
                """.formatted(warehouseId, zoneId));
        UUID locationId = uuid(location, "id");

        String receipt = createResource("/api/receipts", """
                {"reference":"ASN-FLOW-01","supplierId":"%s","lines":[
                  {"productId":"%s","destinationLocationId":"%s","expectedQuantity":10}
                ]}
                """.formatted(supplierId, productId, locationId));
        UUID receiptId = uuid(receipt, "id");
        UUID receiptLineId = uuid(receipt, "lines", 0, "id");

        mockMvc.perform(post("/api/receipts/{id}/receive", receiptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lines":[{"lineId":"%s","quantity":4}]}
                                """.formatted(receiptLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(4));

        mockMvc.perform(post("/api/receipts/{id}/receive", receiptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lines":[{"lineId":"%s","quantity":7}]}
                                """.formatted(receiptLineId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].onHand").value(contains(4)));

        mockMvc.perform(post("/api/receipts/{id}/receive", receiptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lines":[{"lineId":"%s","quantity":6}]}
                                """.formatted(receiptLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(10));

        mockMvc.perform(post("/api/inventory/reorder-point")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","locationId":"%s","reorderPoint":12}
                                """.formatted(productId, locationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.belowReorderPoint").value(true));

        String order = createResource("/api/orders", """
                {"reference":"SO-FLOW-01","lines":[
                  {"productId":"%s","sourceLocationId":"%s","quantity":3}
                ]}
                """.formatted(productId, locationId));
        UUID orderId = uuid(order, "id");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reference":"SO-INSUFFICIENT","lines":[
                                  {"productId":"%s","sourceLocationId":"%s","quantity":8}
                                ]}
                                """.formatted(productId, locationId)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].onHand").value(contains(10)))
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].reserved").value(contains(3)))
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].available").value(contains(7)));

        mockMvc.perform(post("/api/orders/{id}/ship", orderId))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/orders/{id}/pick", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED"));
        mockMvc.perform(post("/api/orders/{id}/pack", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PACKED"));
        mockMvc.perform(post("/api/orders/{id}/ship", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].onHand").value(contains(7)))
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].reserved").value(contains(0)));

        mockMvc.perform(get("/api/inventory/movements")
                        .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'RECEIVE')]").exists())
                .andExpect(jsonPath("$[?(@.type == 'RESERVE')]").exists())
                .andExpect(jsonPath("$[?(@.type == 'PICK')]").exists())
                .andExpect(jsonPath("$[?(@.type == 'SHIP')]").exists());

        String cancellableOrder = createResource("/api/orders", """
                {"reference":"SO-CANCEL","lines":[
                  {"productId":"%s","sourceLocationId":"%s","quantity":2}
                ]}
                """.formatted(productId, locationId));
        UUID cancellableOrderId = uuid(cancellableOrder, "id");
        mockMvc.perform(post("/api/orders/{id}/cancel", cancellableOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].onHand").value(contains(7)))
                .andExpect(jsonPath("$[?(@.locationCode == 'A-01')].reserved").value(contains(0)));
    }

    @Test
    void movesAndAdjustsStockWithoutAllowingNegativeBalances() throws Exception {
        String warehouse = createResource("/api/warehouses", """
                {"code":"WH-MOVE","name":"Move Warehouse","location":"Test"}
                """);
        UUID warehouseId = uuid(warehouse, "id");
        String product = createResource("/api/products", """
                {"sku":"SKU-MOVE","name":"Move Product","unitOfMeasure":"each"}
                """);
        UUID productId = uuid(product, "id");
        UUID sourceLocationId = uuid(createResource("/api/locations", """
                {"warehouseId":"%s","code":"MOVE-A","name":"Move A","type":"STORAGE"}
                """.formatted(warehouseId)), "id");
        UUID destinationLocationId = uuid(createResource("/api/locations", """
                {"warehouseId":"%s","code":"MOVE-B","name":"Move B","type":"STORAGE"}
                """.formatted(warehouseId)), "id");

        mockMvc.perform(post("/api/inventory/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","locationId":"%s","quantity":5}
                                """.formatted(productId, sourceLocationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/inventory/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","sourceLocationId":"%s",
                                 "destinationLocationId":"%s","quantity":2}
                                """.formatted(productId, sourceLocationId, destinationLocationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].onHand").value(3))
                .andExpect(jsonPath("$[1].onHand").value(2));

        mockMvc.perform(post("/api/inventory/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","locationId":"%s","delta":-1}
                                """.formatted(productId, sourceLocationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onHand").value(2));

        mockMvc.perform(post("/api/inventory/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","sourceLocationId":"%s",
                                 "destinationLocationId":"%s","quantity":3}
                                """.formatted(productId, sourceLocationId, destinationLocationId)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.locationCode == 'MOVE-A')].onHand").value(contains(2)))
                .andExpect(jsonPath("$[?(@.locationCode == 'MOVE-B')].onHand").value(contains(2)));
    }

    @Test
    void rejectedTransferLeavesBalancesAndMovementHistoryUnchanged() throws Exception {
        String warehouse = createResource("/api/warehouses", """
                {"code":"WH-ROLLBACK","name":"Rollback Warehouse","location":"Test"}
                """);
        UUID warehouseId = uuid(warehouse, "id");

        String product = createResource("/api/products", """
                {"sku":"SKU-ROLLBACK","name":"Rollback Product","unitOfMeasure":"each"}
                """);
        UUID productId = uuid(product, "id");

        UUID sourceLocationId = uuid(createResource("/api/locations", """
                {"warehouseId":"%s","code":"ROLLBACK-A","name":"Rollback A","type":"STORAGE"}
                """.formatted(warehouseId)), "id");
        UUID destinationLocationId = uuid(createResource("/api/locations", """
                {"warehouseId":"%s","code":"ROLLBACK-B","name":"Rollback B","type":"STORAGE"}
                """.formatted(warehouseId)), "id");

        mockMvc.perform(post("/api/inventory/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","locationId":"%s","quantity":5}
                                """.formatted(productId, sourceLocationId)))
                .andExpect(status().isCreated());

        int movementCountBefore = movementCount(productId);

        mockMvc.perform(post("/api/inventory/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","sourceLocationId":"%s",
                                 "destinationLocationId":"%s","quantity":6}
                                """.formatted(productId, sourceLocationId, destinationLocationId)))
                .andExpect(status().isConflict());

        org.junit.jupiter.api.Assertions.assertEquals(5, inventoryOnHand("ROLLBACK-A"));
        org.junit.jupiter.api.Assertions.assertEquals(0, inventoryOnHand("ROLLBACK-B"));

        org.junit.jupiter.api.Assertions.assertEquals(
                movementCountBefore,
                movementCount(productId),
                "A rejected transfer must not create a stock movement");
    }

    private int inventoryOnHand(String locationCode) throws Exception {
        String response = mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode inventory = objectMapper.readTree(response);
        for (JsonNode row : inventory) {
            if (locationCode.equals(row.path("locationCode").asText())) {
                return row.path("onHand").asInt();
            }
        }
        return 0; // An absent balance row represents zero stock at this location.
    }

    private int movementCount(UUID productId) throws Exception {
        String response = mockMvc.perform(get("/api/inventory/movements")
                        .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).size();
    }

    private String createResource(String path, String body) throws Exception {
        return mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private UUID uuid(String json, String field) throws Exception {
        JsonNode value = objectMapper.readTree(json).path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            throw new AssertionError("Missing " + field + " in response: " + json);
        }
        return UUID.fromString(value.asText());
    }

    private UUID uuid(String json, String array, int index, String field) throws Exception {
        JsonNode value = objectMapper.readTree(json).path(array).path(index).path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            throw new AssertionError("Missing " + array + " line " + index + " field " + field
                    + " in response: " + json);
        }
        return UUID.fromString(value.asText());
    }
}
