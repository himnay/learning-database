-- ============================================================
-- V13: a true PostgreSQL PROCEDURE (not a function)
--
-- Hibernate 6+ invokes StoredProcedureQuery with `CALL ...`, which Postgres
-- only accepts for PROCEDUREs. The V9 get_dept_salary_stats FUNCTION (kept for
-- `SELECT get_...()` native-query demos) cannot be CALLed — so the JPA
-- StoredProcedureQuery demo with IN + multiple OUT params targets this
-- procedure instead. OUT parameters in procedures require PostgreSQL 14+.
-- ============================================================

CREATE OR REPLACE PROCEDURE proc_dept_salary_stats(
    IN  p_dept_id INTEGER,
    OUT p_min_sal NUMERIC,
    OUT p_max_sal NUMERIC,
    OUT p_avg_sal NUMERIC
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT MIN(salary), MAX(salary), AVG(salary)
    INTO   p_min_sal,   p_max_sal,   p_avg_sal
    FROM   employees
    WHERE  dept_id = p_dept_id;
END;
$$;
