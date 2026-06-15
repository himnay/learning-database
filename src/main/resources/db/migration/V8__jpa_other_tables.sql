-- ============================================================
-- Remaining JPA demo tables
-- @EmbeddedId (composite key), Soft Delete, Auditing
-- ============================================================

-- Composite key: (order_id, product_code) together form the PK
-- Covers: @Embeddable, @EmbeddedId
CREATE TABLE jpa_order_item (
    order_id     BIGINT       NOT NULL REFERENCES jpa_order(id),
    product_code VARCHAR(50)  NOT NULL,
    quantity     INTEGER      NOT NULL DEFAULT 1,
    price        NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (order_id, product_code)
);

-- Soft Delete: physically stays in DB, deleted flag prevents visibility
-- Covers: @SQLDelete, @FilterDef, @Filter
CREATE TABLE product (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200)   NOT NULL,
    price      NUMERIC(10, 2) NOT NULL,
    category   VARCHAR(100),
    deleted    BOOLEAN        NOT NULL DEFAULT false,
    created_by       VARCHAR(100),
    created_date     TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP,
    version    BIGINT         NOT NULL DEFAULT 0
);

-- Sample data
INSERT INTO jpa_order_item (order_id, product_code, quantity, price) VALUES
    (1, 'LAP-001', 1, 1200.00),
    (1, 'MON-002', 2,  350.00),
    (2, 'KEY-003', 1,   80.00);

INSERT INTO product (name, price, category, deleted) VALUES
    ('Spring Boot in Action',  45.00,  'Book',         false),
    ('Effective Java',         50.00,  'Book',         false),
    ('Discontinued Widget',    10.00,  'Electronics',  true),
    ('PostgreSQL Guide',       40.00,  'Book',         false),
    ('Old Keyboard',           25.00,  'Electronics',  true);
