-- Heterogeneous graph schema (PostgreSQL 19 docs "myshop" example, extended
-- with a weighted product-recommendation edge): several vertex types
-- (customer, product, order, employee) and several edge types.

CREATE TABLE products (
    product_no int PRIMARY KEY,
    name       text NOT NULL,
    category   text NOT NULL,
    price      numeric(10, 2) NOT NULL
);

CREATE TABLE customers (
    customer_id int PRIMARY KEY,
    name        text NOT NULL,
    address     text NOT NULL
);

CREATE TABLE orders (
    order_id     int PRIMARY KEY,
    ordered_when date NOT NULL
);

CREATE TABLE order_items (
    order_items_id int PRIMARY KEY,
    order_id       int NOT NULL REFERENCES orders (order_id),
    product_no     int NOT NULL REFERENCES products (product_no),
    quantity       int NOT NULL
);

CREATE TABLE customer_orders (
    customer_orders_id int PRIMARY KEY,
    customer_id        int NOT NULL REFERENCES customers (customer_id),
    order_id           int NOT NULL REFERENCES orders (order_id)
);

CREATE TABLE employees (
    employee_id   int PRIMARY KEY,
    employee_name text NOT NULL,
    department    text NOT NULL
);

-- weighted "customers who bought X also bought Y" edge between products
CREATE TABLE also_bought (
    product_id int NOT NULL REFERENCES products (product_no),
    related_id int NOT NULL REFERENCES products (product_no),
    weight     float NOT NULL DEFAULT 1.0,
    PRIMARY KEY (product_id, related_id)
);

INSERT INTO products VALUES
    (1, 'Wireless Headphones', 'Audio',       89.90),
    (2, 'USB-C Charger',       'Accessories', 19.90),
    (3, 'Bluetooth Speaker',   'Audio',       49.90),
    (4, 'Laptop Stand',        'Office',      39.90),
    (5, 'Mechanical Keyboard', 'Office',     129.00);

INSERT INTO customers VALUES
    (1, 'Alice', 'Berlin'),
    (2, 'Bob',   'Paris'),
    (3, 'Carol', 'London');

INSERT INTO orders VALUES
    (1, CURRENT_DATE),
    (2, CURRENT_DATE),
    (3, CURRENT_DATE - 7),
    (4, CURRENT_DATE - 30);

INSERT INTO order_items VALUES
    (1, 1, 1, 1),
    (2, 1, 2, 2),
    (3, 2, 3, 1),
    (4, 3, 5, 1),
    (5, 3, 4, 1),
    (6, 4, 1, 1);

INSERT INTO customer_orders VALUES
    (1, 1, 1),
    (2, 2, 2),
    (3, 3, 3),
    (4, 1, 4);

INSERT INTO employees VALUES
    (1, 'Dave', 'Sales'),
    (2, 'Erin', 'Support');

INSERT INTO also_bought VALUES
    (1, 2, 0.9),   -- headphones -> charger
    (1, 3, 0.7),   -- headphones -> speaker
    (2, 4, 0.4),   -- charger    -> laptop stand
    (3, 5, 0.6),   -- speaker    -> keyboard
    (4, 5, 0.8);   -- stand      -> keyboard
