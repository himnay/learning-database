-- ============================================================
-- Additional columns and tables for new JPA concepts
-- @Convert (Priority enum), @SQLRestriction (StockItem)
-- ============================================================

-- priority column for @Convert / AttributeConverter demo on ProductEntity
ALTER TABLE product ADD COLUMN IF NOT EXISTS priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
UPDATE product SET priority = 'HIGH'   WHERE price >= 50  AND deleted = false;
UPDATE product SET priority = 'LOW'    WHERE price <  45  AND deleted = false;
UPDATE product SET priority = 'NORMAL' WHERE priority = 'NORMAL' AND price >= 45 AND price < 50;

-- stock_item: demonstrates @SQLRestriction (Hibernate 6.3+) as always-on filter
-- Contrast with product which uses @Filter (requires manual session.enableFilter())
CREATE TABLE stock_item (
    id      BIGSERIAL    PRIMARY KEY,
    name    VARCHAR(200) NOT NULL,
    stock   INTEGER      NOT NULL DEFAULT 0,
    deleted BOOLEAN      NOT NULL DEFAULT false
);

INSERT INTO stock_item (name, stock, deleted) VALUES
    ('Widget A',        100, false),
    ('Widget B',         50, false),
    ('Discontinued C',    0, true),
    ('Gadget D',         75, false),
    ('Archived Item',     0, true);
