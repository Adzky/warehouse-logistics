ALTER TABLE warehouse_locations
    ADD CONSTRAINT uq_warehouse_location_id UNIQUE (warehouse_id, id);

ALTER TABLE warehouse_locations
    ADD CONSTRAINT fk_location_parent_same_warehouse
        FOREIGN KEY (warehouse_id, parent_location_id)
        REFERENCES warehouse_locations (warehouse_id, id);
