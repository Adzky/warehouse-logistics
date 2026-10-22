package com.example.warehouse_logistics.exception;

public class DuplicateWarehouseCodeException extends RuntimeException {

    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse code already exists: " + code);
    }
}
