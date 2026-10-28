CREATE TABLE warehouse_users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_warehouse_user_role CHECK (
        role IN ('ADMINISTRATOR', 'WAREHOUSE_MANAGER', 'RECEIVER', 'PICKER')
    )
);

ALTER TABLE stock_movements
    ADD COLUMN performed_by VARCHAR(64) NOT NULL DEFAULT 'system';

ALTER TABLE stock_movements
    ALTER COLUMN performed_by DROP DEFAULT;
