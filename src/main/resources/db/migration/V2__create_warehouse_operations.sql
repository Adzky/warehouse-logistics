CREATE TABLE warehouse_locations (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    location_type VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_warehouse_location_code UNIQUE (warehouse_id, code)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    unit_of_measure VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE inventory_balances (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    location_id UUID NOT NULL REFERENCES warehouse_locations(id),
    on_hand BIGINT NOT NULL DEFAULT 0 CHECK (on_hand >= 0),
    reserved BIGINT NOT NULL DEFAULT 0 CHECK (reserved >= 0 AND reserved <= on_hand),
    reorder_point BIGINT NOT NULL DEFAULT 0 CHECK (reorder_point >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventory_product_location UNIQUE (product_id, location_id)
);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    source_location_id UUID REFERENCES warehouse_locations(id),
    destination_location_id UUID REFERENCES warehouse_locations(id),
    movement_type VARCHAR(32) NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    reference VARCHAR(120),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_stock_movement_location CHECK (
        source_location_id IS NOT NULL OR destination_location_id IS NOT NULL
    )
);

CREATE TABLE inbound_receipts (
    id UUID PRIMARY KEY,
    reference VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE inbound_receipt_lines (
    id UUID PRIMARY KEY,
    receipt_id UUID NOT NULL REFERENCES inbound_receipts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    destination_location_id UUID NOT NULL REFERENCES warehouse_locations(id),
    expected_quantity BIGINT NOT NULL CHECK (expected_quantity > 0),
    received_quantity BIGINT NOT NULL DEFAULT 0
        CHECK (received_quantity >= 0 AND received_quantity <= expected_quantity)
);

CREATE TABLE outbound_orders (
    id UUID PRIMARY KEY,
    reference VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE outbound_order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES outbound_orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    source_location_id UUID NOT NULL REFERENCES warehouse_locations(id),
    quantity BIGINT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX ix_inventory_location ON inventory_balances(location_id);
CREATE INDEX ix_stock_movements_product_time ON stock_movements(product_id, occurred_at);
CREATE INDEX ix_receipt_lines_receipt ON inbound_receipt_lines(receipt_id);
CREATE INDEX ix_order_lines_order ON outbound_order_lines(order_id);
