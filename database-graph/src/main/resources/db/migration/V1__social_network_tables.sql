-- Smallest possible social network (Cybertec SQL/PGQ tutorial):
--   person : vertices
--   knows  : directed edges (who knows whom), with a "since" property

CREATE TABLE person (
    id   int  PRIMARY KEY,
    name text NOT NULL,
    age  int  NOT NULL,
    city text NOT NULL
);

CREATE TABLE knows (
    a     int NOT NULL REFERENCES person (id),
    b     int NOT NULL REFERENCES person (id),
    since int NOT NULL,
    PRIMARY KEY (a, b)
);

INSERT INTO person VALUES
    (1, 'Alice', 30, 'Berlin'),
    (2, 'Bob',   25, 'Berlin'),
    (3, 'Carol', 35, 'Paris'),
    (4, 'Dan',   28, 'Paris'),
    (5, 'Eve',   40, 'London'),
    (6, 'Frank', 33, 'London');

INSERT INTO knows VALUES
    (1, 2, 2018), (2, 1, 2019),
    (1, 3, 2020), (2, 3, 2020), (3, 2, 2021),
    (3, 4, 2021), (4, 5, 2022),
    (5, 6, 2019), (6, 1, 2023);
