package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, UUID> {
    boolean existsByWarehouseIdAndCode(UUID warehouseId, String code);
    List<WarehouseLocation> findByWarehouseId(UUID warehouseId);
}
