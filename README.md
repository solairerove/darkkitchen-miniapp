# darkkitchen-miniapp
TG mini app for poc for dark kitchen

## Local infra (Docker Compose)

The app is expected to run manually (Maven/IntelliJ). Docker Compose is only for infra.

### Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with:

- DB: `darkkitchen`
- User: `darkkitchen`
- Password: `darkkitchen`

These match defaults in `application.yml`:
- `DB_URL=jdbc:postgresql://localhost:5432/darkkitchen`
- `DB_USER=darkkitchen`
- `DB_PASS=darkkitchen`

### Stop infra

```bash
docker compose down
```

### Stop + remove DB volume (fresh DB)

```bash
docker compose down -v
```

## Business logic

### Product Service

- GET /api/products getActiveProducts() → list of active products, sorted by sortOrder (doesn't work)
- GET /api/products/{id} getProduct(id) → return by id, 404 if not found
- POST /api/admin/products createProduct(request) → create new product
- PUT /api/admin/products/{id} updateProduct(id, request) → update, 404 if not found
- DELETE /api/admin/products/{id} deactivateProduct(id) → active = false

to address
- sorting by sortOrder
- pagination
- how to activate back if an item is marked as deleted and doesn't return in the list

### Order Service

telegramUserId is fetched from the request header X-Telegram-User-Id

- POST /api/orders createOrder() → create new order
- GET /api/orders/my getMyOrders(telegramUserId) → return orders for user
- GET /api/orders/{id} .getOrder(telegramUserId, id) → return by id and user
- POST /api/orders/{id}/cancel cancelOrder(telegramUserId, id) → cancel order

to address
- internal server error if user is not found
- pagination

### Admin Service

- GET /api/admin/orders/summary?date=2026-03-08 → get data summary for orders for date or as default tomorrow 
- GET /api/admin/orders?date=2026-03-08 → get all orders for date or as default tomorrow
- PUT /api/admin/orders/{id}/status → update order status

## Security

Admin endpoints are protected with Spring Security:

- `/api/admin/**`
- `/admin/**` (reserved for Thymeleaf admin pages)

Authentication options:

- Form login at `/login` (for browser/Thymeleaf flows)
- HTTP Basic auth (for API clients/scripts)

Credentials are configured via environment variables:

- `ADMIN_USERNAME` (default: `admin`)
- `ADMIN_PASSWORD` (default: `admin123`)
