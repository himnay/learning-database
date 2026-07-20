-- ============================================================
-- V11: @SoftDelete demo table + unique constraint for upsert
-- Covers: Hibernate @SoftDelete (6.4+), ON CONFLICT upsert target,
--         "adding a unique constraint" (ALTER TABLE ... ADD CONSTRAINT)
-- ============================================================

-- Unique constraint on product.name — required as the ON CONFLICT target
-- for the native upsert in ProductRepository.upsertProduct()
ALTER TABLE product ADD CONSTRAINT product_name_unique UNIQUE (name);

-- note: demonstrates Hibernate @SoftDelete — the `deleted` column is managed
-- entirely by Hibernate and is NOT mapped as an entity field
CREATE TABLE note (
    id      BIGSERIAL    PRIMARY KEY,
    title   VARCHAR(200) NOT NULL,
    content TEXT,
    deleted BOOLEAN      NOT NULL DEFAULT false
);

INSERT INTO note (title, content, deleted) VALUES
    ('JPA cheatsheet',      'CascadeType, FetchType, mappedBy rules', false),
    ('Interview questions', 'N+1, locking, isolation levels',         false),
    ('Old draft',           'superseded content',                     true);
