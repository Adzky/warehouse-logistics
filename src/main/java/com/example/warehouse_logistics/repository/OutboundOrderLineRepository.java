package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.OutboundOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboundOrderLineRepository extends JpaRepository<OutboundOrderLine, UUID> {
    boolean existsBySourceLocationId(UUID locationId);
}
