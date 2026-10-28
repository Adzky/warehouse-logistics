package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.CreateUserRequest;
import com.example.warehouse_logistics.dto.UserResponse;
import com.example.warehouse_logistics.entity.WarehouseUser;
import com.example.warehouse_logistics.exception.DuplicateResourceException;
import com.example.warehouse_logistics.repository.WarehouseUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class UserAccountService {

    private final WarehouseUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(WarehouseUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return users.findAllByOrderByUsernameAsc().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
        }
        WarehouseUser user = new WarehouseUser(username,
                passwordEncoder.encode(request.password()), request.role());
        return UserResponse.from(users.save(user));
    }
}
