-- ============================================================
-- JPA Inheritance strategy demo tables
-- 1. SINGLE_TABLE  → vehicle (Car + Motorcycle in one table, dtype discriminator)
-- 2. JOINED        → payment + credit_card_payment + bank_transfer_payment
-- 3. MappedSuperclass → computer + mobile_phone (no parent table)
-- 4. TABLE_PER_CLASS  → dog + cat each have all columns, shared sequence for IDs
-- ============================================================

-- 1. SINGLE_TABLE ─ one table, discriminator column "dtype"
--    Car-only:        num_doors
--    Motorcycle-only: engine_capacity_cc
CREATE TABLE vehicle (
    id                 BIGSERIAL PRIMARY KEY,
    dtype              VARCHAR(31)  NOT NULL,
    brand              VARCHAR(100) NOT NULL,
    model              VARCHAR(100) NOT NULL,
    num_doors          INTEGER,
    engine_capacity_cc INTEGER
);

-- 2. JOINED ─ parent + child tables, child PK is also FK to parent
CREATE TABLE payment (
    id           BIGSERIAL PRIMARY KEY,
    amount       NUMERIC(10, 2) NOT NULL,
    payment_date DATE           NOT NULL,
    dtype        VARCHAR(31)    NOT NULL
);

CREATE TABLE credit_card_payment (
    id          BIGINT PRIMARY KEY REFERENCES payment(id),
    card_number VARCHAR(20)  NOT NULL,
    card_holder VARCHAR(100) NOT NULL
);

CREATE TABLE bank_transfer_payment (
    id             BIGINT PRIMARY KEY REFERENCES payment(id),
    bank_name      VARCHAR(100) NOT NULL,
    account_number VARCHAR(50)  NOT NULL
);

-- 3. MappedSuperclass ─ no parent table, each concrete entity gets its own table
CREATE TABLE computer (
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    name  VARCHAR(100) NOT NULL,
    os    VARCHAR(50)
);

CREATE TABLE mobile_phone (
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    name  VARCHAR(100) NOT NULL,
    color VARCHAR(50)
);

-- 4. TABLE_PER_CLASS ─ separate table per concrete class, shared sequence for unique IDs
CREATE SEQUENCE animal_seq START 1 INCREMENT 1;

CREATE TABLE dog (
    id    BIGINT DEFAULT nextval('animal_seq') PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    breed VARCHAR(100)
);

CREATE TABLE cat (
    id    BIGINT DEFAULT nextval('animal_seq') PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    color VARCHAR(50)
);

-- Sample data
INSERT INTO vehicle (dtype, brand, model, num_doors) VALUES
    ('Car', 'Toyota', 'Camry', 4),
    ('Car', 'Honda',  'Civic', 2);

INSERT INTO vehicle (dtype, brand, model, engine_capacity_cc) VALUES
    ('Motorcycle', 'Yamaha', 'R1',    1000),
    ('Motorcycle', 'Honda',  'CBR600', 600);

INSERT INTO payment (amount, payment_date, dtype) VALUES (250.00, CURRENT_DATE, 'CreditCard');
INSERT INTO credit_card_payment (id, card_number, card_holder) VALUES (1, '4111111111111111', 'Alice Smith');

INSERT INTO payment (amount, payment_date, dtype) VALUES (1200.00, CURRENT_DATE, 'BankTransfer');
INSERT INTO bank_transfer_payment (id, bank_name, account_number) VALUES (2, 'HSBC', 'GB12HSBC1234567890');

INSERT INTO computer (brand, name, os) VALUES
    ('Apple', 'MacBook Pro', 'macOS'),
    ('Dell',  'XPS 15',      'Windows 11');

INSERT INTO mobile_phone (brand, name, color) VALUES
    ('Apple',   'iPhone 15',      'Midnight'),
    ('Samsung', 'Galaxy S24',     'Cream');

INSERT INTO dog (name, breed) VALUES ('Rex', 'German Shepherd'), ('Buddy', 'Labrador');
INSERT INTO cat (name, color) VALUES ('Whiskers', 'Orange'), ('Luna', 'Black');
