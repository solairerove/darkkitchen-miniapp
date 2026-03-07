CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    unit        VARCHAR(50) NOT NULL DEFAULT 'шт',
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE customer_order (
    id                BIGSERIAL PRIMARY KEY,
    telegram_user_id  BIGINT NOT NULL,
    customer_name     VARCHAR(255) NOT NULL,
    customer_phone    VARCHAR(50),
    delivery_date     DATE NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    note              TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE order_item (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES product(id),
    quantity    INT NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_delivery_date ON customer_order(delivery_date);
CREATE INDEX idx_order_status ON customer_order(status);
CREATE INDEX idx_order_telegram_user ON customer_order(telegram_user_id);
