package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.InboundReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboundReceiptLineRepository extends JpaRepository<InboundReceiptLine, UUID> {
    boolean existsByDestinationLocationId(UUID locationId);
}
