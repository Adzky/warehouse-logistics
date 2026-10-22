# Full-stack warehouse logistics development plan

## Starting point

This repository is a small Java 21 / Spring Boot application with PostgreSQL, Spring Data JPA, Flyway, validation, and web support configured in `pom.xml`. It already has a basic warehouse CRUD backend:

- A `Warehouse` entity with an ID, code, name, and location in `Warehouse.java`.
- A repository, service, and REST controller in `WarehouseRepository.java`, `WarehouseService.java`, and `WarehouseController.java`.
- A PostgreSQL connection configuration in `application.properties`.

This is currently a backend starting point, not a full logistics application: inventory, products, locations within warehouses, receiving, orders, and a user interface are not present yet. The existing controller also needs a consistent HTTP response contract: its create handler currently defaults to `200 OK`, while the test expects `201 Created` in `WarehouseControllerTest.java`.

## Recommended direction

Build a **Java-first web application** using Spring Boot for the backend and Spring MVC with Thymeleaf templates for server-rendered pages. This keeps the main application and UI rendering in Java while enabling interactive screens with small amounts of browser-side JavaScript where useful. Use PostgreSQL for persistent data and Flyway migrations for schema changes.

## Phased roadmap

### 1. Stabilize the foundation

- Confirm local development and test setup for Java 21, Maven, and PostgreSQL.
- Resolve the create-endpoint status mismatch and define consistent API behavior for success and error responses.
- Add request/response DTOs so HTTP input is not bound directly to JPA entities.
- Add validation for required fields and formats; return clear, consistent validation and not-found errors.
- Use Flyway migrations as the schema source of truth; validate the schema at startup rather than relying on Hibernate to update it.
- Add integration-test database configuration and document environment-based database settings.

**Outcome:** Reliable startup, predictable endpoint behavior, and a safe foundation for evolving the database.

### 2. Define the warehouse domain and core workflows

**Status: Implemented as an API MVP.** The Java/Spring domain currently includes:

1. Warehouses, root zones and operational locations, and bins nested under zones.
2. Products with unique SKUs, descriptions, units of measure, and active status; suppliers can be associated with inbound receipts.
3. Inventory balances by product and location, with on-hand, reserved, available, and configurable reorder-point quantities.
4. Auditable receive, move, adjustment, reserve, release, pick, and ship movements.
5. Inbound receipts with expected quantities and repeatable partial-receipt operations.
6. Outbound orders that reserve stock and transition through pick, pack, ship, or pre-pick cancellation.

The API is under `/api`: `/warehouses`, `/locations`, `/products`, `/suppliers`,
`/inventory`, `/inventory/movements`, `/receipts`, and `/orders`. Inventory
updates use transactions, product-level locking, row locking, and PostgreSQL
constraints to prevent negative or over-reserved balances. Receipt and order
operations write movement history with timestamps and references.

Carrier integrations and user attribution remain future work; user attribution
depends on the authentication work in Phase 4. The web interface is planned in
Phase 3.

### 3. Build the application interface

**Status: Implemented.** Server-rendered Thymeleaf pages now cover the first useful workflows. The REST API under `/api` remains available.

Create Thymeleaf pages for the first useful workflows:

- Dashboard: inventory alerts, inbound receipts, and orders awaiting fulfillment.
- Warehouse and location management.
- Product search and inventory-by-location views.
- Receiving and put-away forms.
- Order creation, picking, packing, and shipment status.
- Movement history and basic operational reports.

Add search, filtering, pagination, form validation feedback, and confirmation for destructive actions. Keep accessibility and mobile-friendly layouts in scope for warehouse-floor usage.

**Outcome:** Staff can perform essential operations through a browser, not just API calls.

### 4. Add identity, authorization, and auditability

- Add Spring Security with login and role-based authorization.
- Define roles such as administrator, warehouse manager, receiver, and picker.
- Restrict operations by role and, if needed, by warehouse.
- Record who performed each stock-affecting action and when.
- Protect state-changing requests against CSRF and avoid exposing credentials in source control.

**Outcome:** Operations are appropriately restricted and changes can be traced.

### 5. Expand testing and operational readiness

- Unit-test service rules such as duplicate warehouse codes, stock reservations, and invalid movements.
- Add integration tests for repositories, migrations, and transaction behavior.
- Add MVC tests for authorization, validation, response codes, and core workflows.
- Test concurrency-sensitive inventory updates and failure/rollback behavior.
- Add structured logging, health checks, and useful error reporting.
- Package the app for repeatable local or container-based deployment and document backup, configuration, and migration practices.

**Outcome:** The app can be tested and operated beyond a developer’s local machine.

## Suggested milestone order

1. **Foundation:** stabilize current warehouse CRUD, response contracts, migrations, and tests.
2. **Inventory MVP:** products, locations, inventory balances, and auditable stock movements.
3. **Operational MVP:** receiving, picking, order fulfillment, and a usable web dashboard.
4. **Production readiness:** security roles, audit coverage, deployment, monitoring, and performance improvements.

Keep the first release focused on warehouse, product, inventory, and movement workflows. Add procurement, carrier integrations, barcode scanning, and advanced reporting after the core stock records and workflows are reliable.