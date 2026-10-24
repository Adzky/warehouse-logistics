package com.example.warehouse_logistics.config;

import com.example.warehouse_logistics.entity.UserRole;
import com.example.warehouse_logistics.entity.WarehouseUser;
import com.example.warehouse_logistics.repository.WarehouseUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class BootstrapAdministrator {

    @Bean
    CommandLineRunner bootstrapAdministratorRunner(
            WarehouseUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${warehouse.security.bootstrap-admin-username:}") String username,
            @Value("${warehouse.security.bootstrap-admin-password:}") String password) {
        return args -> createAdministrator(users, passwordEncoder, username, password);
    }

    void createAdministrator(WarehouseUserRepository users, PasswordEncoder passwordEncoder,
                             String username, String password) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Set both BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD, or neither");
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.length() > 64
                || !normalizedUsername.matches("[a-z0-9._-]+")) {
            throw new IllegalStateException(
                    "Bootstrap administrator username must use letters, digits, dots, underscores, or hyphens");
        }
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Bootstrap administrator password must be 12-72 UTF-8 bytes");
        }
        if (!users.existsByUsername(normalizedUsername)) {
            users.save(new WarehouseUser(normalizedUsername,
                    passwordEncoder.encode(password), UserRole.ADMINISTRATOR));
        }
    }
}
