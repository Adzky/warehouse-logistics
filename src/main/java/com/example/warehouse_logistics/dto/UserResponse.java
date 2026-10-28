package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.UserRole;
import com.example.warehouse_logistics.entity.WarehouseUser;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String username, UserRole role, boolean enabled, Instant createdAt) {
    public static UserResponse from(WarehouseUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(),
                user.isEnabled(), user.getCreatedAt());
    }
}
