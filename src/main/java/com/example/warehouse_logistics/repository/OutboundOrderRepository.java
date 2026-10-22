package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.OutboundOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, UUID> {
    boolean existsByReference(String reference);

    List<OutboundOrder> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select outboundOrder from OutboundOrder outboundOrder where outboundOrder.id = :id")
    Optional<OutboundOrder> findForUpdate(@Param("id") UUID id);
}
