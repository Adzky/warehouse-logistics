package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.InboundReceipt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, UUID> {
    boolean existsByReference(String reference);

    List<InboundReceipt> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select receipt from InboundReceipt receipt where receipt.id = :id")
    Optional<InboundReceipt> findForUpdate(@Param("id") UUID id);
}
