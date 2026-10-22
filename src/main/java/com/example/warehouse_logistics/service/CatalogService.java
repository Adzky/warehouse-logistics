package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.LocationRequest;
import com.example.warehouse_logistics.dto.LocationResponse;
import com.example.warehouse_logistics.dto.ProductRequest;
import com.example.warehouse_logistics.dto.ProductResponse;
import com.example.warehouse_logistics.dto.SupplierRequest;
import com.example.warehouse_logistics.dto.SupplierResponse;
import com.example.warehouse_logistics.entity.Product;
import com.example.warehouse_logistics.entity.LocationType;
import com.example.warehouse_logistics.entity.Supplier;
import com.example.warehouse_logistics.entity.Warehouse;
import com.example.warehouse_logistics.entity.WarehouseLocation;
import com.example.warehouse_logistics.exception.DuplicateResourceException;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.repository.ProductRepository;
import com.example.warehouse_logistics.repository.SupplierRepository;
import com.example.warehouse_logistics.repository.WarehouseLocationRepository;
import com.example.warehouse_logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {

    private final ProductRepository products;
    private final WarehouseRepository warehouses;
    private final WarehouseLocationRepository locations;
    private final SupplierRepository suppliers;

    public CatalogService(ProductRepository products, WarehouseRepository warehouses,
                          WarehouseLocationRepository locations, SupplierRepository suppliers) {
        this.products = products;
        this.warehouses = warehouses;
        this.locations = locations;
        this.suppliers = suppliers;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }
        return ProductResponse.from(products.save(new Product(
                request.sku(), request.name(), request.description(), request.unitOfMeasure())));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findProducts() {
        return products.findAll().stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        Warehouse warehouse = warehouses.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));
        WarehouseLocation parent = request.parentLocationId() == null
                ? null
                : locations.findById(request.parentLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent location", request.parentLocationId()));
        if (parent != null && !parent.getWarehouse().getId().equals(warehouse.getId())) {
            throw new IllegalArgumentException("Parent location must belong to the same warehouse");
        }
        if (request.type() == LocationType.BIN
                && (parent == null || parent.getType() != LocationType.ZONE)) {
            throw new IllegalArgumentException("A bin must be assigned to a zone");
        }
        if (request.type() != LocationType.BIN && parent != null) {
            throw new IllegalArgumentException("Only bins can be nested under a parent location");
        }
        if (locations.existsByWarehouseIdAndCode(request.warehouseId(), request.code())) {
            throw new DuplicateResourceException("Location code already exists in this warehouse");
        }
        return LocationResponse.from(locations.save(new WarehouseLocation(
                warehouse, parent, request.code(), request.name(), request.type())));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> findLocations(UUID warehouseId) {
        List<WarehouseLocation> results = warehouseId == null
                ? locations.findAll()
                : locations.findByWarehouseId(warehouseId);
        return results.stream().map(LocationResponse::from).toList();
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (suppliers.existsByCode(request.code())) {
            throw new DuplicateResourceException("Supplier code already exists: " + request.code());
        }
        return SupplierResponse.from(suppliers.save(new Supplier(
                request.code(), request.name(), request.contactEmail())));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findSuppliers() {
        return suppliers.findAll().stream().map(SupplierResponse::from).toList();
    }
}
