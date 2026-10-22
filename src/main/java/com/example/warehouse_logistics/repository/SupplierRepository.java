package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByCode(String code);
}
