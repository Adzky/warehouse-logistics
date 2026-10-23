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
import com.example.warehouse_logistics.repository.InventoryBalanceRepository;
import com.example.warehouse_logistics.repository.InboundReceiptLineRepository;
import com.example.warehouse_logistics.repository.OutboundOrderLineRepository;
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
    private final InventoryBalanceRepository balances;
    private final InboundReceiptLineRepository receiptLines;
    private final OutboundOrderLineRepository orderLines;

    public CatalogService(ProductRepository products, WarehouseRepository warehouses,
                          WarehouseLocationRepository locations, SupplierRepository suppliers,
                          InventoryBalanceRepository balances, InboundReceiptLineRepository receiptLines,
                          OutboundOrderLineRepository orderLines) {
        this.products = products;
        this.warehouses = warehouses;
        this.locations = locations;
        this.suppliers = suppliers;
        this.balances = balances;
        this.receiptLines = receiptLines;
        this.orderLines = orderLines;
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

    @Transactional(readOnly = true)
    public ProductResponse findProduct(UUID id) {
        return products.findById(id).map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (products.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }
        product.update(request.sku(), request.name(), request.description(), request.unitOfMeasure());
        return ProductResponse.from(product);
    }

    @Transactional
    public void setProductActive(UUID id, boolean active) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(active);
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

    @Transactional(readOnly = true)
    public LocationResponse findLocation(UUID id) {
        return locations.findById(id).map(LocationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }

    @Transactional
    public LocationResponse updateLocation(UUID id, String code, String name) {
        WarehouseLocation location = locations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
        if (locations.existsByWarehouseIdAndCodeAndIdNot(location.getWarehouse().getId(), code, id)) {
            throw new DuplicateResourceException("Location code already exists in this warehouse");
        }
        location.update(code, name);
        return LocationResponse.from(location);
    }

    @Transactional
    public void setLocationActive(UUID id, boolean active) {
        WarehouseLocation location = locations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
        if (!active && (locations.existsByParentLocationId(id)
                || balances.existsByLocationIdAndOnHandGreaterThan(id, 0)
                || balances.existsByLocationIdAndReservedGreaterThan(id, 0)
                || receiptLines.existsByDestinationLocationId(id)
                || orderLines.existsBySourceLocationId(id))) {
            throw new IllegalStateException(
                    "Location cannot be archived while it has stock, child locations, or receipt/order references");
        }
        location.setActive(active);
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

    @Transactional(readOnly = true)
    public SupplierResponse findSupplier(UUID id) {
        return suppliers.findById(id).map(SupplierResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    @Transactional
    public SupplierResponse updateSupplier(UUID id, SupplierRequest request) {
        Supplier supplier = suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        if (suppliers.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Supplier code already exists: " + request.code());
        }
        supplier.update(request.code(), request.name(), request.contactEmail());
        return SupplierResponse.from(supplier);
    }

    @Transactional
    public void setSupplierActive(UUID id, boolean active) {
        Supplier supplier = suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setActive(active);
    }
}
