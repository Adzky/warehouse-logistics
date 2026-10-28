package com.example.warehouse_logistics.controller;

import com.example.warehouse_logistics.dto.CreateUserRequest;
import com.example.warehouse_logistics.dto.UserResponse;
import com.example.warehouse_logistics.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {

    private final UserAccountService service;

    public UserAccountController(UserAccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return service.create(request);
    }
}
