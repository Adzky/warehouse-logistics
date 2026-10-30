package com.example.warehouse_logistics;

import com.example.warehouse_logistics.dto.ProductRequest;
import com.example.warehouse_logistics.dto.StockReceiptRequest;
import com.example.warehouse_logistics.dto.WarehouseRequest;
import com.example.warehouse_logistics.service.CatalogService;
import com.example.warehouse_logistics.service.InventoryService;
import com.example.warehouse_logistics.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private WarehouseService warehouses;

    @Autowired
    private CatalogService catalog;

    @Autowired
    private InventoryService inventory;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void concurrentReservationsCannotReserveMoreThanAvailable() throws Exception {
        String unique = UUID.randomUUID().toString();
        UUID warehouseId = null;
        UUID productId = null;
        try {
            var warehouse = warehouses.create(new WarehouseRequest(
                    "WH-CONC-" + unique, "Concurrency Test", "Test"));
            warehouseId = warehouse.id();
            var product = catalog.createProduct(new ProductRequest(
                    "SKU-CONC-" + unique, "Concurrency Test", null, "each"));
            productId = product.id();
            UUID locationId = catalog.findLocations(warehouseId).getFirst().id();
            UUID reservedProductId = productId;

            inventory.receive(new StockReceiptRequest(
                    reservedProductId, locationId, 10, "CONC-SETUP"));

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            try (var executor = Executors.newFixedThreadPool(2)) {
                var first = executor.submit(() ->
                        attemptReservation(ready, start, reservedProductId, locationId, "CONC-1"));
                var second = executor.submit(() ->
                        attemptReservation(ready, start, reservedProductId, locationId, "CONC-2"));

                assertTrue(ready.await(5, TimeUnit.SECONDS), "Reservation threads did not become ready");
                start.countDown();
                int successfulReservations = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                        + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

                assertEquals(1, successfulReservations);
                var balance = inventory.getInventory().stream()
                        .filter(row -> row.productId().equals(reservedProductId))
                        .findFirst()
                        .orElseThrow();
                assertEquals(6, balance.reserved());
                assertEquals(4, balance.available());
            }
        } finally {
            if (productId != null) {
                jdbc.update("DELETE FROM stock_movements WHERE product_id = ?", productId);
                jdbc.update("DELETE FROM inventory_balances WHERE product_id = ?", productId);
                jdbc.update("DELETE FROM products WHERE id = ?", productId);
            }
            if (warehouseId != null) {
                jdbc.update("DELETE FROM warehouse_locations WHERE warehouse_id = ?", warehouseId);
                jdbc.update("DELETE FROM warehouses WHERE id = ?", warehouseId);
            }
        }
    }

    private boolean attemptReservation(CountDownLatch ready, CountDownLatch start,
                                       UUID productId, UUID locationId, String reference)
            throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Reservation start signal timed out");
        }
        try {
            inventory.reserve(productId, locationId, 6, reference);
            return true;
        } catch (IllegalStateException insufficientStock) {
            return false;
        }
    }
}
