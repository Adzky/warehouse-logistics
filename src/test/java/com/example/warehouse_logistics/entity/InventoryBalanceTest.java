package com.example.warehouse_logistics.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryBalanceTest {

    @Test
    void reservationAndShipmentPreserveAvailableStock() {
        InventoryBalance balance = new InventoryBalance(null, null);
        balance.receive(10);
        balance.reserve(6);

        assertEquals(4, balance.available());
        assertThrows(IllegalStateException.class, () -> balance.reserve(5));

        balance.ship(2);

        assertEquals(8, balance.getOnHand());
        assertEquals(4, balance.getReserved());
        assertEquals(4, balance.available());
    }

    @Test
    void adjustmentCannotReduceStockBelowReservations() {
        InventoryBalance balance = new InventoryBalance(null, null);
        balance.receive(10);
        balance.reserve(4);

        assertThrows(IllegalStateException.class, () -> balance.adjust(-7));

        assertEquals(10, balance.getOnHand());
        assertEquals(4, balance.getReserved());
    }

    @Test
    void receivingBeyondSupportedQuantityFailsWithoutChangingStock() {
        InventoryBalance balance = new InventoryBalance(null, null);
        balance.receive(Long.MAX_VALUE);

        assertThrows(ArithmeticException.class, () -> balance.receive(1));

        assertEquals(Long.MAX_VALUE, balance.getOnHand());
    }
}
