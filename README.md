# Warehouse Logistics

A Java 21 / Spring Boot and PostgreSQL application for managing warehouse
catalogs, location-based inventory, receiving, and outbound order fulfillment.
The project provides a REST API and a browser UI for the core warehouse
workflows. User authentication is not implemented yet.

## Current scope

- Warehouse records and warehouse locations.
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

- Java 21
- Spring Boot 4
- Spring MVC, Thymeleaf, Spring Data JPA, Jakarta Validation
- PostgreSQL
- Flyway
- Maven Wrapper

## Prerequisites

- JDK 21
- PostgreSQL running locally
- A PostgreSQL database named `warehouse_logistics`, or an equivalent database
  configured in `src/main/resources/application.properties`

Review the datasource URL, username, and password in
`src/main/resources/application.properties` and set them to match your local
PostgreSQL setup. Do not commit database credentials.

## Run locally

From PowerShell in the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Open
[http://localhost:8080](http://localhost:8080) for the warehouse dashboard.
Flyway applies migrations
from `src/main/resources/db/migration`; Hibernate uses `validate` mode and does
not generate or update the schema.

For an existing, non-empty database without Flyway history, Flyway is configured
to baseline it at version `0` before applying the migrations. Review your
database and back it up before running migrations against important data.

## Run tests

```powershell
.\mvnw.cmd test
```

Integration tests use the PostgreSQL datasource configured for the application.
They exercise warehouse CRUD responses, request validation, duplicate codes,
partial receiving, inventory reservation and fulfillment, location transfers,
stock adjustment, movement history, and rollback behavior when an operation is
rejected.

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

List pages support search or status filters and pagination. Create forms show
field-level validation messages. Delete warehouse, cancel order, ship, and
negative stock adjustment ask for confirmation before submitting.

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
