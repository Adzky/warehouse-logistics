package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.InventoryBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select balance from InventoryBalance balance " +
            "where balance.product.id = :productId and balance.location.id = :locationId")
    Optional<InventoryBalance> findForUpdate(
            @Param("productId") UUID productId,
            @Param("locationId") UUID locationId);

    List<InventoryBalance> findAllByOrderByLocation_CodeAsc();
    boolean existsByLocationIdAndOnHandGreaterThan(UUID locationId, long quantity);
    boolean existsByLocationIdAndReservedGreaterThan(UUID locationId, long quantity);
}
