package com.example.warehouse_logistics.service;

import com.example.warehouse_logistics.dto.InventoryResponse;
import com.example.warehouse_logistics.dto.ReorderPointRequest;
import com.example.warehouse_logistics.dto.StockAdjustmentRequest;
import com.example.warehouse_logistics.dto.StockMoveRequest;
import com.example.warehouse_logistics.dto.StockMovementResponse;
import com.example.warehouse_logistics.dto.StockReceiptRequest;
import com.example.warehouse_logistics.entity.InventoryBalance;
import com.example.warehouse_logistics.entity.MovementType;
import com.example.warehouse_logistics.entity.Product;
import com.example.warehouse_logistics.entity.StockMovement;
import com.example.warehouse_logistics.entity.WarehouseLocation;
import com.example.warehouse_logistics.exception.ResourceNotFoundException;
import com.example.warehouse_logistics.repository.InventoryBalanceRepository;
import com.example.warehouse_logistics.repository.ProductRepository;
import com.example.warehouse_logistics.repository.StockMovementRepository;
import com.example.warehouse_logistics.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository products;
    private final WarehouseLocationRepository locations;
    private final InventoryBalanceRepository balances;
    private final StockMovementRepository movements;
    private final CurrentActor currentActor;

    public InventoryService(ProductRepository products, WarehouseLocationRepository locations,
                            InventoryBalanceRepository balances, StockMovementRepository movements,
                            CurrentActor currentActor) {
        this.products = products;
        this.locations = locations;
        this.balances = balances;
        this.movements = movements;
        this.currentActor = currentActor;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory() {
        return balances.findAllByOrderByLocation_CodeAsc().stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovements(UUID productId) {
        List<StockMovement> result = productId == null
                ? movements.findAllByOrderByOccurredAtDesc()
                : movements.findByProductIdOrderByOccurredAtDesc(productId);
        return result.stream().map(StockMovementResponse::from).toList();
    }

    @Transactional
    public InventoryResponse receive(StockReceiptRequest request) {
        Product product = lockProduct(request.productId());
        WarehouseLocation location = requireLocation(request.locationId());
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.receive(request.quantity());
        movements.save(new StockMovement(product, null, location, MovementType.RECEIVE,
                request.quantity(), request.reference(), currentActor.username()));
        return InventoryResponse.from(balance);
    }

    @Transactional
    public List<InventoryResponse> move(StockMoveRequest request) {
        if (request.sourceLocationId().equals(request.destinationLocationId())) {
            throw new IllegalArgumentException("Source and destination locations must differ");
        }
        Product product = lockProduct(request.productId());
        WarehouseLocation source = requireLocation(request.sourceLocationId());
        WarehouseLocation destination = requireLocation(request.destinationLocationId());
        InventoryBalance from = balanceForUpdate(product, source);
        InventoryBalance to = balanceForUpdate(product, destination);
        from.adjust(-request.quantity());
        to.receive(request.quantity());
        movements.save(new StockMovement(product, source, destination, MovementType.MOVE,
                request.quantity(), request.reference(), currentActor.username()));
        return List.of(InventoryResponse.from(from), InventoryResponse.from(to));
    }

    @Transactional
    public InventoryResponse adjust(StockAdjustmentRequest request) {
        if (request.delta() == 0) {
            throw new IllegalArgumentException("Adjustment delta cannot be zero");
        }
        if (request.delta() == Long.MIN_VALUE) {
            throw new IllegalArgumentException("Adjustment delta is outside the supported range");
        }
        Product product = lockProduct(request.productId());
        WarehouseLocation location = requireLocation(request.locationId());
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.adjust(request.delta());
        movements.save(new StockMovement(product,
                request.delta() < 0 ? location : null,
                request.delta() > 0 ? location : null,
                request.delta() > 0 ? MovementType.ADJUST_IN : MovementType.ADJUST_OUT,
                Math.abs(request.delta()), request.reference(), currentActor.username()));
        return InventoryResponse.from(balance);
    }

    @Transactional
    public InventoryResponse setReorderPoint(ReorderPointRequest request) {
        Product product = lockProduct(request.productId());
        WarehouseLocation location = requireLocation(request.locationId());
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.setReorderPoint(request.reorderPoint());
        return InventoryResponse.from(balance);
    }

    @Transactional
    public void reserve(UUID productId, UUID locationId, long quantity, String reference) {
        Product product = lockProduct(productId);
        WarehouseLocation location = requireLocation(locationId);
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.reserve(quantity);
        movements.save(new StockMovement(product, null, location, MovementType.RESERVE,
                quantity, reference, currentActor.username()));
    }

    @Transactional
    public void pick(UUID productId, UUID locationId, long quantity, String reference) {
        Product product = lockProduct(productId);
        WarehouseLocation location = requireLocation(locationId);
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.pick(quantity);
        movements.save(new StockMovement(product, location, null, MovementType.PICK,
                quantity, reference, currentActor.username()));
    }

    @Transactional
    public void ship(UUID productId, UUID locationId, long quantity, String reference) {
        Product product = lockProduct(productId);
        WarehouseLocation location = requireLocation(locationId);
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.ship(quantity);
        movements.save(new StockMovement(product, location, null, MovementType.SHIP,
                quantity, reference, currentActor.username()));
    }

    @Transactional
    public void release(UUID productId, UUID locationId, long quantity, String reference) {
        Product product = lockProduct(productId);
        WarehouseLocation location = requireLocation(locationId);
        InventoryBalance balance = balanceForUpdate(product, location);
        balance.release(quantity);
        movements.save(new StockMovement(product, location, null, MovementType.RELEASE,
                quantity, reference, currentActor.username()));
    }

    private Product lockProduct(UUID productId) {
        return products.findForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private WarehouseLocation requireLocation(UUID locationId) {
        return locations.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }

    private InventoryBalance balanceForUpdate(Product product, WarehouseLocation location) {
        return balances.findForUpdate(product.getId(), location.getId())
                .orElseGet(() -> balances.save(new InventoryBalance(product, location)));
    }
}
