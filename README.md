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

### ProductService

- GET /api/products getActiveProducts() → list of active products, sorted by sortOrder (doesn't work)
- GET /api/products/{id} getProduct(id) → return by id, 404 if not found
- POST /api/admin/products createProduct(request) → create new product
- PUT /api/admin/products/{id} updateProduct(id, request) → update, 404 if not found
- DELETE /api/admin/products/{id} deactivateProduct(id) → active = false

to address
- sorting by sortOrder
- pagination
- how to activate back if an item is marked as deleted and doesn't return in the list