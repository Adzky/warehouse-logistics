INSERT INTO warehouse_locations (id, warehouse_id, code, name, location_type)
SELECT gen_random_uuid(), warehouse.id, 'MAIN', 'Main Storage', 'STORAGE'
FROM warehouses warehouse
WHERE NOT EXISTS (
    SELECT 1
    FROM warehouse_locations location
    WHERE location.warehouse_id = warehouse.id
);
