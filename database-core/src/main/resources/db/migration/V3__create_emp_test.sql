-- FAQ Q3: ROW_NUMBER() vs RANK() vs DENSE_RANK()
-- FAQ Q4: Find nth highest salary
-- FAQ Q5: nth highest salary with index (PostgreSQL uses LIMIT instead of Oracle ROWNUM)
CREATE TABLE emp_test (
    emp_name VARCHAR(10),
    salary   NUMERIC(10, 2)
);

INSERT INTO emp_test (emp_name, salary) VALUES
    ('A', 5000),
    ('B', 4000),
    ('C', 4000),
    ('D', 3000);

-- Index for efficient nth-highest salary queries (FAQ Q5)
-- PostgreSQL equivalent of Oracle: CREATE INDEX idx_emp_salary_desc ON Employees (salary DESC)
CREATE INDEX idx_emp_test_salary_desc ON emp_test (salary DESC);
