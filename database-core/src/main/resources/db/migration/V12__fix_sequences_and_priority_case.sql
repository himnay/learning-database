-- ============================================================
-- V12: data fixes surfaced by API testing
-- ============================================================

-- V1/V2 seeded departments/employees with EXPLICIT ids, which does not advance
-- the SERIAL sequence — the next generated id collided with seeded rows
-- (duplicate key on employees_pkey). Re-sync the sequences past the seed data.
SELECT setval(pg_get_serial_sequence('departments', 'dept_id'), (SELECT MAX(dept_id) FROM departments));
SELECT setval(pg_get_serial_sequence('employees',   'emp_id'),  (SELECT MAX(emp_id)  FROM employees));

-- V10 stored priority as 'HIGH'/'NORMAL'/'LOW' but PriorityConverter WRITES
-- lowercase ('high'), so findByPriority(HIGH) never matched the seeded rows.
-- Normalize to the converter's canonical form.
UPDATE product SET priority = LOWER(priority);
