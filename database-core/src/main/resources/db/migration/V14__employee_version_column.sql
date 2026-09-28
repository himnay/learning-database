-- ============================================================
-- V14: optimistic-locking version column on employees
--
-- EmployeeRepository.findByEmail uses @Lock(LockModeType.OPTIMISTIC), which
-- needs a @Version attribute: without one Hibernate 7 fails the query with
-- "Entity ... has no version and may not be locked at level OPTIMISTIC".
-- Existing rows start at 0; Hibernate increments it on every entity UPDATE.
-- (JPQL bulk updates such as updateSalaryByDepartment do NOT touch it.)
-- ============================================================

ALTER TABLE employees ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
