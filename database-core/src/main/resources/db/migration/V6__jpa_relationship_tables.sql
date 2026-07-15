-- ============================================================
-- JPA Relationship demo tables
-- Covers: @OneToOne, @OneToMany/@ManyToOne, @ManyToMany
--         bidirectional / unidirectional, @JoinColumn, @JoinTable
--         CascadeType, orphanRemoval, FetchType
-- ============================================================

-- @OneToOne: jpa_user owns the FK (address_id)
CREATE TABLE jpa_address (
    id      BIGSERIAL PRIMARY KEY,
    street  VARCHAR(200) NOT NULL,
    city    VARCHAR(100) NOT NULL,
    zip     VARCHAR(20)
);

CREATE TABLE jpa_user (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    address_id BIGINT UNIQUE REFERENCES jpa_address(id)
);

-- @OneToMany / @ManyToOne: customer (1) -> orders (N), FK in order
CREATE TABLE jpa_customer (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE
);

CREATE TABLE jpa_order (
    id          BIGSERIAL PRIMARY KEY,
    product     VARCHAR(200) NOT NULL,
    amount      NUMERIC(10, 2) NOT NULL,
    customer_id BIGINT REFERENCES jpa_customer(id)
);

-- @ManyToMany: student <-> course via join table
CREATE TABLE jpa_student (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE
);

CREATE TABLE jpa_course (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT
);

CREATE TABLE jpa_student_course (
    student_id BIGINT NOT NULL REFERENCES jpa_student(id),
    course_id  BIGINT NOT NULL REFERENCES jpa_course(id),
    PRIMARY KEY (student_id, course_id)
);

-- Sample data
INSERT INTO jpa_address (street, city, zip) VALUES
    ('123 Main St', 'New York', '10001'),
    ('456 Oak Ave', 'Los Angeles', '90001');

INSERT INTO jpa_user (name, email, address_id) VALUES
    ('Alice Smith', 'alice@example.com', 1),
    ('Bob Jones',   'bob@example.com',   2);

INSERT INTO jpa_customer (name, email) VALUES
    ('Acme Corp', 'acme@corp.com'),
    ('Globex Ltd', 'globex@ltd.com');

INSERT INTO jpa_order (product, amount, customer_id) VALUES
    ('Laptop',  1200.00, 1),
    ('Monitor',  350.00, 1),
    ('Keyboard',  80.00, 2);

INSERT INTO jpa_student (name, email) VALUES
    ('Priya Sharma', 'priya@edu.com'),
    ('Rahul Mehta',  'rahul@edu.com'),
    ('Aarav Patel',  'aarav@edu.com');

INSERT INTO jpa_course (title, description) VALUES
    ('Spring Boot Fundamentals', 'Introduction to Spring Boot'),
    ('Database Design',          'SQL, JPA, and Hibernate'),
    ('Microservices',            'Designing distributed systems');

INSERT INTO jpa_student_course (student_id, course_id) VALUES
    (1, 1), (1, 2),
    (2, 2), (2, 3),
    (3, 1), (3, 3);
