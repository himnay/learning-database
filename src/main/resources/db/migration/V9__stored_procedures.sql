-- ============================================================
-- PostgreSQL stored functions for JPA stored procedure demos
-- Covers: @NamedStoredProcedureQuery, @Procedure, StoredProcedureQuery
-- ============================================================

-- Simple scalar function — called via @Query(nativeQuery=true) or @Procedure
CREATE OR REPLACE FUNCTION get_total_employees()
RETURNS INTEGER AS $$
BEGIN
    RETURN (SELECT COUNT(*)::INTEGER FROM employees);
END;
$$ LANGUAGE plpgsql;

-- Function with IN param and scalar OUT
CREATE OR REPLACE FUNCTION get_employee_count_by_dept(p_dept_id INTEGER)
RETURNS INTEGER AS $$
BEGIN
    RETURN (SELECT COUNT(*)::INTEGER FROM employees WHERE dept_id = p_dept_id);
END;
$$ LANGUAGE plpgsql;

-- Function with multiple OUT params — returns salary stats for a department
-- Called via StoredProcedureQuery or @Query(nativeQuery=true)
CREATE OR REPLACE FUNCTION get_dept_salary_stats(
    p_dept_id  IN  INTEGER,
    p_min_sal  OUT NUMERIC,
    p_max_sal  OUT NUMERIC,
    p_avg_sal  OUT NUMERIC
) AS $$
BEGIN
    SELECT MIN(salary), MAX(salary), AVG(salary)
    INTO   p_min_sal,   p_max_sal,   p_avg_sal
    FROM   employees
    WHERE  dept_id = p_dept_id;
END;
$$ LANGUAGE plpgsql;
