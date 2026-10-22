ALTER TABLE warehouse_locations
    ADD COLUMN parent_location_id UUID REFERENCES warehouse_locations(id);

CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    contact_email VARCHAR(255)
);

ALTER TABLE inbound_receipts
    ADD COLUMN supplier_id UUID REFERENCES suppliers(id);

CREATE INDEX ix_warehouse_location_parent ON warehouse_locations(parent_location_id);
CREATE INDEX ix_receipt_supplier ON inbound_receipts(supplier_id);
