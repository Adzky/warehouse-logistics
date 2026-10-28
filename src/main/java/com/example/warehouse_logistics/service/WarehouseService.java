package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.WarehouseRequest;
import com.example.warehouse_logistics.dto.WarehouseResponse;
import com.example.warehouse_logistics.entity.LocationType;
import com.example.warehouse_logistics.entity.Warehouse;
import com.example.warehouse_logistics.entity.WarehouseLocation;
import com.example.warehouse_logistics.exception.DuplicateWarehouseCodeException;
import com.example.warehouse_logistics.exception.WarehouseNotFoundException;
import com.example.warehouse_logistics.repository.WarehouseRepository;
import com.example.warehouse_logistics.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseLocationRepository locationRepository;

    public WarehouseService(WarehouseRepository repository, WarehouseLocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateWarehouseCodeException(request.code());
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(request.code());
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        Warehouse savedWarehouse = repository.save(warehouse);
        locationRepository.save(new WarehouseLocation(
                savedWarehouse, null, "MAIN", "Main Storage", LocationType.STORAGE));
        return WarehouseResponse.from(savedWarehouse);
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

    @Transactional
    public void delete(UUID id) {
        Warehouse warehouse = repository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        List<WarehouseLocation> warehouseLocations = locationRepository.findByWarehouseId(id);
        if (warehouseLocations.size() == 1 && "MAIN".equals(warehouseLocations.getFirst().getCode())) {
            locationRepository.delete(warehouseLocations.getFirst());
        }
        repository.delete(warehouse);
    }
}