-- FAQ Q10: Find restaurant with >= 4 ratings and never rated below 4
CREATE TABLE deliveries (
    order_id   INTEGER PRIMARY KEY,
    restaurant VARCHAR(100)   NOT NULL,
    amount     NUMERIC(10, 2) NOT NULL,
    rating     INTEGER        NOT NULL CHECK (rating BETWEEN 1 AND 5)
);

INSERT INTO deliveries (order_id, restaurant, amount, rating) VALUES
    (9,  'Taco Town',    300, 5),
    (1,  'Pizza Palace', 450, 5),
    (3,  'Pizza Palace', 510, 5),
    (4,  'Burger Barn',  200, 5),
    (6,  'Sushi Star',   600, 4),
    (2,  'Pizza Palace', 320, 4),
    (7,  'Sushi Star',   550, 4),
    (8,  'Sushi Star',   480, 4),
    (10, 'Taco Town',    150, 3),
    (5,  'Burger Barn',  180, 2);
