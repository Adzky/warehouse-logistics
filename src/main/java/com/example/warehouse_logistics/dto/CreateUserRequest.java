package com.example.warehouse_logistics.dto;

import com.example.warehouse_logistics.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9._-]+")
        String username,
        @NotBlank
        @Size(min = 12, max = 64)
        String password,
        @NotNull UserRole role
) {
}
