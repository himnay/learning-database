-- ============================================================
--  DB Interview Queries — PostgreSQL
--  Run these after: docker compose up -d && mvn spring-boot:run
-- ============================================================

-- ============================================================
-- Q1: Employee and Department Problem
-- ============================================================

-- Simple JOIN
SELECT *
FROM employees e
JOIN departments d ON e.dept_id = d.dept_id;

-- Number of employees in each department
SELECT d.dept_name, COUNT(*) AS num_employees
FROM employees e
JOIN departments d ON e.dept_id = d.dept_id
GROUP BY d.dept_name
HAVING COUNT(*) > 0;

-- Highest salary across all departments
SELECT MAX(salary) AS highest_salary
FROM employees
JOIN departments d ON employees.dept_id = d.dept_id;

-- Highest salary in each department
SELECT d.dept_name, MAX(e.salary) AS highest_salary
FROM employees e
JOIN departments d ON e.dept_id = d.dept_id
GROUP BY d.dept_name;


-- ============================================================
-- Q2: Find departments with more than X employees
-- ============================================================

-- ORDER OF EXECUTION: FROM -> WHERE -> GROUP BY -> HAVING -> SELECT
-- WHERE cannot reference aggregates — use HAVING instead

-- Departments with more than 2 employees
SELECT dept_id, COUNT(*) AS num_employees
FROM employees
GROUP BY dept_id
HAVING COUNT(*) > 2;


-- ============================================================
-- Q3: ROW_NUMBER() vs RANK() vs DENSE_RANK()
-- ============================================================

-- ROW_NUMBER : always unique, no ties
-- RANK       : ties get same rank, next rank skips (gaps)
-- DENSE_RANK : ties get same rank, no gaps

SELECT
    emp_name,
    salary,
    ROW_NUMBER()  OVER (ORDER BY salary DESC) AS row_num,
    RANK()        OVER (ORDER BY salary DESC) AS rnk,
    DENSE_RANK()  OVER (ORDER BY salary DESC) AS dense_rnk
FROM emp_test
ORDER BY salary DESC, emp_name;

-- Expected result:
-- emp_name | salary | row_num | rnk | dense_rnk
-- A        | 5000   | 1       | 1   | 1
-- B        | 4000   | 2       | 2   | 2
-- C        | 4000   | 3       | 2   | 2
-- D        | 3000   | 4       | 4   | 3


-- ============================================================
-- Q4: Find the Nth highest salary (using DENSE_RANK)
-- ============================================================

-- Replace N with desired rank (e.g. 2 for 2nd highest)
SELECT emp_name, salary, dense_rnk
FROM (
    SELECT
        emp_name,
        salary,
        DENSE_RANK() OVER (ORDER BY salary DESC) AS dense_rnk
    FROM emp_test
) ranked
WHERE dense_rnk = 2;   -- change 2 to any N


-- ============================================================
-- Q5: Nth highest salary — efficient way using index
-- ============================================================
-- Oracle uses ROWNUM; PostgreSQL uses LIMIT

-- Index already created in V3 migration: idx_emp_test_salary_desc
-- Query using LIMIT (PostgreSQL equivalent of Oracle ROWNUM approach)
SELECT MIN(salary) AS nth_highest_salary
FROM (
    SELECT DISTINCT salary
    FROM emp_test
    ORDER BY salary DESC
    LIMIT 2           -- replace 2 with N
) top_n;


-- ============================================================
-- Q6: Pivot Table — rows to columns with CASE + GROUP BY
-- ============================================================

-- Original: (name, subject, score)
-- Target  : (name, math, science, english)

-- Rule: every non-aggregated column in SELECT must be in GROUP BY

SELECT
    name,
    MAX(CASE WHEN subject = 'Math'    THEN score END) AS math,
    MAX(CASE WHEN subject = 'Science' THEN score END) AS science,
    MAX(CASE WHEN subject = 'English' THEN score END) AS english
FROM scores
GROUP BY name
ORDER BY name;


-- ============================================================
-- Q7: Swap two columns without a temp table
-- ============================================================
-- PostgreSQL evaluates the RHS before updating, so this is safe

UPDATE employees
SET col_a = col_b,
    col_b = col_a;

-- Verify swap
SELECT emp_id, first_name, col_a, col_b FROM employees LIMIT 3;

-- Swap back
UPDATE employees
SET col_a = col_b,
    col_b = col_a;


-- ============================================================
-- Q8: NTILE() — divide rows into N equal buckets
-- ============================================================

-- Divide employees into 4 salary quartiles
SELECT
    first_name,
    salary,
    NTILE(4) OVER (ORDER BY salary DESC) AS quartile
FROM employees
ORDER BY salary DESC;


-- ============================================================
-- Q9: LAG() and LEAD() — access previous / next row
-- ============================================================

-- LAG  : looks at the row BEFORE the current row
-- LEAD : looks at the row AFTER the current row

SELECT
    first_name,
    salary,
    LAG(first_name) OVER (ORDER BY salary DESC)  AS prev_emp_name,
    LAG(salary)     OVER (ORDER BY salary DESC)  AS prev_salary,
    LEAD(first_name) OVER (ORDER BY salary DESC) AS next_emp_name,
    LEAD(salary)     OVER (ORDER BY salary DESC) AS next_salary
FROM employees
ORDER BY salary DESC;


-- ============================================================
-- Q10: Find restaurants with >= 4 ratings and no rating below 4
-- ============================================================

SELECT restaurant, COUNT(*) AS total_ratings, MIN(rating) AS min_rating
FROM deliveries
GROUP BY restaurant
HAVING COUNT(*) >= 4
   AND MIN(rating) >= 4;

-- Expected: Sushi Star (4 ratings, min=4), Pizza Palace (3 ❌ — only 3 ratings)
-- Note: Pizza Palace gets filtered out because COUNT(*) = 3 < 4
