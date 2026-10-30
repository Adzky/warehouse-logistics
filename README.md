# Warehouse Logistics

A Java 25 / Spring Boot and PostgreSQL application for managing warehouse
catalogs, location-based inventory, receiving, and outbound order fulfillment.
The project provides a REST API and a browser UI for the core warehouse
workflows. Authentication uses database-backed accounts, role-based access
control, CSRF protection, and actor attribution for stock movements.

## Current scope

- Warehouse records and warehouse locations. Each new warehouse starts with a
  root `MAIN` storage location for receiving and inventory; additional zones,
  bins, and operational locations can be created as needed.
- Root locations for zones and operational areas; bins nested under zones.
- Products with unique SKUs and suppliers associated with inbound receipts.
- Inventory balances by product and location: on-hand, reserved, available,
  and reorder point.
- Stock movement history for receiving, moving, adjusting, reserving,
  releasing, picking, and shipping.
- Inbound receipts with partial receiving.
- Outbound orders with reservation, pick, pack, ship, and pre-pick cancellation.
- Database migrations managed with Flyway; Hibernate validates the schema at
  startup.

## Technology

- Java 25 (LTS)
- Spring Boot 4
- Spring MVC, Thymeleaf, Spring Data JPA, Jakarta Validation
- PostgreSQL
- Flyway
- Maven Wrapper

## Prerequisites

- JDK 25 (LTS)
- PostgreSQL running locally
- A PostgreSQL database named `warehouse_logistics`, or an equivalent database
  configured in `src/main/resources/application.properties`

### Initialize PostgreSQL

1. Install PostgreSQL and start its service. The default application datasource
   expects PostgreSQL at `localhost:5432`.
2. Connect to the built-in `postgres` database as a PostgreSQL administrator.
   From PowerShell, for example:

   ```powershell
   psql -h localhost -p 5432 -U postgres -d postgres
   ```

   Enter the PostgreSQL administrator password when prompted. If `psql` is not
   on `PATH`, run it from PostgreSQL's `bin` directory or use pgAdmin's Query
   Tool connected to the `postgres` database.
3. Create an empty application database. The default application username is
   `postgres`; if you use a different application username, replace the owner
   below with that role:

   ```sql
   CREATE DATABASE warehouse_logistics OWNER postgres;
   ```

   Run this once; PostgreSQL reports an error if the database already exists.
   For an existing database, do not drop or recreate it. Confirm that the
   configured application role can connect and create tables in its schema.
4. Configure the application to use the database. The default URL is
   `jdbc:postgresql://localhost:5432/warehouse_logistics`, and the default
   username is `postgres`. If using the local profile, create an ignored
   `application-local.properties` file in the repository root and set the
   password variable expected by the datasource configuration:

   ```properties
   DB_PASSWORD=your-local-postgres-password
   ```

   Alternatively, set `DB_PASSWORD` in the process environment before starting
   Maven. Keep local credentials out of source control. If your PostgreSQL host,
   port, database, or username differs, update the datasource settings in
   `src/main/resources/application.properties` to match.
   On a fresh database or the first startup after enabling security, set
   `BOOTSTRAP_ADMIN_USERNAME` and
   `BOOTSTRAP_ADMIN_PASSWORD` in the application process environment before
   starting it. The initial administrator password must be at least 12
   characters and at most 72 UTF-8 bytes. The account is created only if its
   username does not already exist; changing the environment variable later
   does not reset its password.
5. Verify the database exists and that the configured application credentials
   can connect. For the defaults:

   ```powershell
   psql -h localhost -p 5432 -U postgres -d warehouse_logistics -c "SELECT current_database(), current_user;"
   ```

   The result should show `warehouse_logistics` and the application role.

On the first application startup, Flyway creates the tables by applying
migrations from `src/main/resources/db/migration`. Hibernate validates the
resulting schema; it does not create or update tables. Keep the database empty
for a fresh setup. Flyway is configured to baseline an existing non-empty
database without migration history, so review and back up important existing
data before starting the application against it.

## Run locally

From PowerShell in the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Open
[http://localhost:8080](http://localhost:8080) for the warehouse dashboard.
Sign in at `/login`. An administrator can list and create accounts with
`GET /api/users` and `POST /api/users`; account creation requires an
administrator session, a CSRF token, and a JSON body containing `username`,
`password` (12-64 characters and at most 72 UTF-8 bytes), and a role (`ADMINISTRATOR`,
`WAREHOUSE_MANAGER`, `RECEIVER`, or `PICKER`). Passwords are stored as BCrypt
hashes and are never included in account responses.

Administrators and warehouse managers can manage catalogs, warehouses,
inventory, receipts, and orders. Receivers can view records and process
receipts/stock receiving. Pickers can view records and pick, pack, and ship
orders; they cannot create or cancel orders. All browser and API state changes
require CSRF protection. Stock movement records retain the username of the
authenticated actor and the movement timestamp.
When serving the application over HTTPS (including through a TLS-terminating
proxy), set `SESSION_COOKIE_SECURE=true` so browsers send the session cookie
only over secure connections.

## Health, logs, and deployment

The application exposes only `/actuator/health` over HTTP. It is intentionally
public for container health checks; component details are not exposed. The
database health indicator makes the endpoint report `DOWN` when PostgreSQL is
unavailable. Application logs are emitted as Logstash-compatible JSON on
standard output.

For a repeatable local container deployment:

1. Copy `.env.example` to `.env` and replace every placeholder. In PowerShell:

   ```powershell
   Copy-Item .env.example .env
   ```

   Use unique, strong values for the database and bootstrap administrator
   credentials.
   Keep `.env` private; it is ignored by Git and excluded from Docker builds.
2. Start the app and PostgreSQL:

   ```powershell
   docker compose up --build -d
   docker compose ps
   docker compose logs -f app
   ```

3. Open `http://localhost:8080` and sign in with the bootstrap administrator
   credentials in `.env`. The bootstrap account is created only if its
   username does not already exist; changing `.env` does not reset its
   password.
4. Stop the services with `docker compose down`. The named PostgreSQL volume
   remains intact. Do not use `docker compose down -v` unless you intentionally
   want to delete the database.

This Compose setup is for local development, not a hardened public production
deployment. For production, use managed secret storage, private database
networking, TLS, restricted inbound access, and a tested backup/restore process.
The image runs as a non-root user and does not include local profile properties.

### Database backups and migrations

Take and verify a database backup before upgrades or risky operations. For the
Compose database shown above, with PostgreSQL client tools installed on the host,
create a custom-format backup:

```powershell
pg_dump -h localhost -p 5432 -U warehouse_app -d warehouse_logistics -W -Fc `
  -f warehouse-logistics.dump
```

Restore into a separately created, empty database after stopping the app:

```powershell
pg_restore -h localhost -p 5432 -U warehouse_app -d warehouse_logistics_restore `
  -W --no-owner --no-acl warehouse-logistics.dump
```

Keep backups encrypted and outside the app host, and periodically test restores.
`pg_dump` backs up one database; preserve database roles/configuration separately.

Flyway applies pending migrations at application startup. Give each new migration
a unique, increasing version (for example `V8__describe_change.sql`), and never
edit a migration that has already been applied in a shared environment. Make
schema changes with forward-only migrations, test them against a disposable
database, and back up production data before deploying. Hibernate is configured
to validate, not create or update, the schema.

## Run tests

```powershell
.\mvnw.cmd test
```

Integration tests use the PostgreSQL datasource configured for the application.
They exercise warehouse CRUD responses, request validation, duplicate codes,
partial receiving, inventory reservation and fulfillment, location transfers,
stock adjustment, movement history, and rollback behavior when an operation is
rejected.
The inventory domain has unit tests for reservation, adjustment, and overflow
rules. A concurrent-reservation integration test verifies that two simultaneous
requests cannot reserve more stock than is available.

The workflow tests use a transaction that rolls back test data when each test
finishes. The tests still require a reachable PostgreSQL database and apply
pending Flyway migrations at startup.

## Browser UI

Server-rendered pages use Spring MVC and Thymeleaf. They call the same
application services as the REST API.

| Page | Path | Purpose |
|---|---|---|
| Dashboard | `/` | Inventory alerts, open receipts, and orders awaiting fulfillment |
| Warehouses | `/warehouses` | Search, create, and open warehouses |
| Warehouse detail | `/warehouses/{id}` | Manage zones, bins, and operational locations |
| Products | `/products` | Search the catalog and create SKUs |
| Suppliers | `/suppliers` | Maintain suppliers used on inbound receipts |
| Inventory | `/inventory` | Inventory by location, receive, move, adjust, reorder points |
| Receiving | `/receipts` | Create inbound receipts and put away stock |
| Receipt detail | `/receipts/{id}` | Receive outstanding line quantities |
| Orders | `/orders` | Create orders that reserve stock |
| Order detail | `/orders/{id}` | Pick, pack, ship, or cancel |
| Movements | `/movements` | Movement history and basic operational counts |

List pages support search or status filters and pagination. Create and edit
forms show field-level validation messages. Product, supplier, and location
detail pages support editing and archive/restore actions; locations with stock,
child locations, or receipt/order references cannot be archived. Warehouse
deletion, archiving records, order cancellation/shipping, and stock adjustment
ask for confirmation before submitting.

## REST API

All endpoints use the `/api` prefix and JSON request/response bodies.

| Resource | Method and path | Purpose |
|---|---|---|
| Warehouses | `POST /api/warehouses` | Create a warehouse |
| Warehouses | `GET /api/warehouses` | List warehouses |
| Warehouses | `GET /api/warehouses/{id}` | Get a warehouse |
| Warehouses | `DELETE /api/warehouses/{id}` | Delete a warehouse |
| Products | `POST /api/products` | Create a product |
| Products | `GET /api/products` | List products |
| Suppliers | `POST /api/suppliers` | Create a supplier |
| Suppliers | `GET /api/suppliers` | List suppliers |
| Locations | `POST /api/locations` | Create a warehouse location |
| Locations | `GET /api/locations` | List locations; optionally filter by `warehouseId` |
| Inventory | `GET /api/inventory` | List balances |
| Inventory | `POST /api/inventory/receive` | Receive stock directly into a location |
| Inventory | `POST /api/inventory/move` | Transfer stock between locations |
| Inventory | `POST /api/inventory/adjust` | Apply a positive or negative stock adjustment |
| Inventory | `POST /api/inventory/reorder-point` | Set a balance's reorder point |
| Movements | `GET /api/inventory/movements` | List movements; optionally filter by `productId` |
| Receipts | `POST /api/receipts` | Create an inbound receipt |
| Receipts | `GET /api/receipts` | List inbound receipts |
| Receipts | `POST /api/receipts/{id}/receive` | Receive some or all outstanding receipt quantities |
| Orders | `POST /api/orders` | Create an order and reserve its stock |
| Orders | `GET /api/orders` | List outbound orders |
| Orders | `POST /api/orders/{id}/pick` | Mark a reserved order as picked |
| Orders | `POST /api/orders/{id}/pack` | Mark a picked order as packed |
| Orders | `POST /api/orders/{id}/ship` | Ship a packed order and deduct on-hand/reserved stock |
| Orders | `POST /api/orders/{id}/cancel` | Cancel a reserved order and release its reservation |

Creation endpoints return `201 Created`. Warehouse deletion returns `204 No
Content`. Invalid requests return `400 Bad Request`, missing resources return
`404 Not Found`, and duplicate records, inventory conflicts, or invalid order
transitions return `409 Conflict`. Error bodies use Spring's Problem Details
format; validation errors include an `errors` object keyed by field.

### Location rules

`LocationType` values are `ZONE`, `BIN`, `RECEIVING`, `STORAGE`, `PICKING`, and
`SHIPPING`. Create a `ZONE` as a root location, then create bins with its
`parentLocationId`. A bin's parent must be a zone in the same warehouse.
Operational locations are root locations. Location codes must be unique within
their warehouse.

Example location request:

```json
{
  "warehouseId": "WAREHOUSE_UUID",
  "parentLocationId": "ZONE_UUID",
  "code": "A-01",
  "name": "Storage bin A-01",
  "type": "BIN"
}
```

### Inbound receiving example

Create a receipt with expected line quantities:

```json
{
  "reference": "ASN-1001",
  "supplierId": "SUPPLIER_UUID",
  "lines": [
    {
      "productId": "PRODUCT_UUID",
      "destinationLocationId": "LOCATION_UUID",
      "expectedQuantity": 10
    }
  ]
}
```

Post to `/api/receipts/{receiptId}/receive` with the returned line ID and the
quantity being received. Repeated requests are supported until the expected
quantity is reached. Receiving more than the outstanding quantity is rejected.
Receipt statuses are `OPEN`, `PARTIALLY_RECEIVED`, and `RECEIVED`.

```json
{
  "lines": [
    {
      "lineId": "RECEIPT_LINE_UUID",
      "quantity": 4
    }
  ]
}
```

### Outbound fulfillment example

Create an order with one or more product/location quantities. The application
reserves available stock when creating the order and rejects requests that
cannot be reserved:

```json
{
  "reference": "SO-1001",
  "lines": [
    {
      "productId": "PRODUCT_UUID",
      "sourceLocationId": "LOCATION_UUID",
      "quantity": 3
    }
  ]
}
```

Progress the returned order ID through:

```text
POST /api/orders/{orderId}/pick
POST /api/orders/{orderId}/pack
POST /api/orders/{orderId}/ship
```

Order statuses are `RESERVED`, `PICKED`, `PACKED`, `SHIPPED`, and `CANCELLED`.
Only a reserved order can be canceled; cancellation releases its reservation.
Stock remains reserved through picking and packing, then shipping decreases
both on-hand and reserved quantities.

### Inventory and movement history

An inventory response reports `onHand`, `reserved`, `available`, and
`reorderPoint`. Available quantity is on-hand minus reserved. The
`belowReorderPoint` flag is true when available quantity is less than or equal
to the configured reorder point.

The adjustment endpoint accepts a non-zero signed `delta`. Transfers and
adjustments cannot reduce stock below reserved quantities or make on-hand stock
negative. Operations run transactionally: if a transfer fails, neither balance
changes nor movement records are committed.

Movement history includes product, type, quantity, source and/or destination
location, reference, and timestamp. Filter it by product using:

```text
GET /api/inventory/movements?productId=PRODUCT_UUID
```

## Database migrations

Flyway SQL migration files are stored in
`src/main/resources/db/migration`. Current migrations create the warehouse
operations schema, location hierarchy, supplier records, and same-warehouse
parent-location constraint. Add future schema changes as new versioned
migrations; do not edit migrations that have already run in a shared database.

## Tests

`WarehouseControllerTest` covers warehouse API creation, invalid input,
malformed JSON, duplicate warehouse codes, listing, and missing records.
`WebUiTest` covers dashboard and list-page rendering, warehouse form
validation and creation, and the HTML not-found page.
`WarehouseWorkflowTest` covers:

- Partial receipt updates, over-receipt rejection, and receipt completion.
- Reorder-point reporting and low-inventory visibility.
- Reservation and insufficient-stock rejection.
- Pick, pack, ship, cancellation, and stock/movement updates.
- Transfers, negative adjustments, and insufficient-stock rejection.
- Transaction rollback: a rejected transfer leaves both balances and movement
  history unchanged.

## Current limitations

- Authentication, authorization, and movement attribution to a user are not
  implemented.
- Carrier integrations, supplier purchasing, barcode scanning, and advanced
  reporting are not included. List pages paginate in the web layer.
- Catalog and location resources currently expose create/list operations rather
  than full update/archive workflows.
