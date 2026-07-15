-- FAQ Q1, Q2: departments table used in JOIN and GROUP BY / HAVING queries
CREATE TABLE departments (
    dept_id   SERIAL PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL
);

INSERT INTO departments (dept_id, dept_name) VALUES
    (1, 'Engineering'),
    (2, 'Marketing'),
    (3, 'Sales'),
    (4, 'HR');
