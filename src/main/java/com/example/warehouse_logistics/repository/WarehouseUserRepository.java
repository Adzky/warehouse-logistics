package com.example.warehouse_logistics.repository;

import com.example.warehouse_logistics.entity.WarehouseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, UUID> {
    Optional<WarehouseUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<WarehouseUser> findAllByOrderByUsernameAsc();
}
