package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.WarehouseRequest;
import com.example.warehouse_logistics.dto.WarehouseResponse;
import com.example.warehouse_logistics.entity.Warehouse;
import com.example.warehouse_logistics.exception.DuplicateWarehouseCodeException;
import com.example.warehouse_logistics.exception.WarehouseNotFoundException;
import com.example.warehouse_logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseRepository repository;

    public WarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    public WarehouseResponse create(WarehouseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateWarehouseCodeException(request.code());
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(request.code());
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        return WarehouseResponse.from(repository.save(warehouse));
    }

    public List<WarehouseResponse> findAll() {
        return repository.findAll().stream()
                .map(WarehouseResponse::from)
                .toList();
    }

    public WarehouseResponse findById(UUID id) {
        return repository.findById(id)
                .map(WarehouseResponse::from)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    public void delete(UUID id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id)));
    }
}