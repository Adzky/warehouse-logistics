package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WarehouseRepository
        extends JpaRepository<Warehouse, UUID> {
    boolean existsByCode(String code);
}