## Order Processing System (Spring Boot)

Production-grade Spring Boot (Java 17) Order Processing System demonstrating layered architecture, clean code, scheduler-based state progression, and unit test coverage.

## Architecture

- **controller**: REST API layer; accepts/returns DTOs only.
- **service**: Business logic + transaction boundaries.
- **repository**: Spring Data JPA repositories.
- **model**: JPA entities + `OrderStatus` enum.
- **dto**: Request/response models for API.
- **scheduler**: Periodic job that progresses order state.
- **exception**: Domain exceptions + global exception handler.

## API Endpoints

- **Create order**: `POST /orders`
  - Body: `CreateOrderRequest`
  - Returns: `201 Created` + `OrderResponse`
- **Get order by id**: `GET /orders/{id}`
  - Returns: `200 OK` + `OrderResponse`
- **Get orders by status**: `GET /orders?status=PENDING`
  - Returns: `200 OK` + `List<OrderResponse>`
- **Update order status**: `PUT /orders/{id}/status`
  - Body: `UpdateOrderStatusRequest` (contains `status`)
  - Returns: `200 OK` + `OrderResponse`
- **Cancel order**: `DELETE /orders/{id}`
  - Returns: `204 No Content`

## Business Rules

- **Order creation**: orders always start as **`PENDING`**.
- **Order cancel**: allowed only when status is **`PENDING`**.
  - Otherwise an `InvalidOrderStateException` is thrown and mapped to **HTTP 409**.
- **Price snapshot**: `OrderItem.price` is stored as a snapshot at order creation time.

## Scheduler

The scheduler runs **every 5 minutes** and performs:

- Fetch all orders with status **`PENDING`**
- Update them to **`PROCESSING`**
- Runs via `OrderService.processPendingOrders()` under a transaction for consistency

## Exception Handling

Global exception handling is implemented using `@ControllerAdvice`:

- `OrderNotFoundException` → **404**
- `InvalidOrderStateException` → **409**
- Validation/type mismatch → **400**
- Fallback → **500**

## How to Run

Run the app:

```bash
./mvnw spring-boot:run
```

H2 Console:

- Path: `/h2-console`
- JDBC URL: `jdbc:h2:mem:orderprocessing`

Run tests:

```bash
./mvnw test
```

## Design Decisions & Trade-offs

- **DTO-only controller contracts**: avoids leaking persistence concerns and makes API evolution safer.
- **Service as transactional boundary**: keeps business rules centralized and consistent.
- **Scheduler calls service method**: preserves the same business + transaction semantics used by APIs.
- **H2 in-memory DB**: convenient local/dev runtime; swap to Postgres/MySQL by changing datasource config.

