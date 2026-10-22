package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findByProductIdOrderByOccurredAtDesc(UUID productId);
    List<StockMovement> findAllByOrderByOccurredAtDesc();
}
