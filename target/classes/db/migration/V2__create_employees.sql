-- FAQ Q1, Q2: JOIN, GROUP BY, HAVING, MAX(salary)
-- FAQ Q7: Swap columns (col_a, col_b added for that exercise)
-- FAQ Q8: NTILE()
-- FAQ Q9: LAG() / LEAD()
CREATE TABLE employees (
    emp_id     SERIAL PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    email      VARCHAR(100),
    salary     NUMERIC(10, 2) NOT NULL,
    dept_id    INTEGER        REFERENCES departments (dept_id),
    col_a      VARCHAR(50)    DEFAULT 'value_A',
    col_b      VARCHAR(50)    DEFAULT 'value_B'
);

-- Engineering: 4 employees
INSERT INTO employees (emp_id, first_name, last_name, email, salary, dept_id) VALUES
    (1,  'Alice',  'Murphy', 'alice.murphy@example.com',  80000.00, 1),
    (2,  'Bob',    'Singh',  'bob.singh@example.com',     75000.00, 1),
    (3,  'Eve',    'Carter', 'eve.carter@example.com',    90000.00, 1),
    (4,  'Frank',  'Lee',    'frank.lee@example.com',     85000.00, 1),
-- Marketing: 2 employees
    (5,  'Clara',  'Jones',  'clara.jones@example.com',   60000.00, 2),
    (6,  'Grace',  'Patel',  'grace.patel@example.com',   55000.00, 2),
-- Sales: 3 employees
    (7,  'David',  'Khan',   'david.khan@example.com',    65000.00, 3),
    (8,  'Henry',  'Zhao',   'henry.zhao@example.com',    70000.00, 3),
    (9,  'Iris',   'Nair',   'iris.nair@example.com',     62000.00, 3),
-- HR: 1 employee
    (10, 'Jack',   'Brown',  'jack.brown@example.com',    50000.00, 4);
