-- FAQ Q6: Pivot table — transform rows (name, subject, score) into columns
CREATE TABLE scores (
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(50)    NOT NULL,
    subject VARCHAR(50)    NOT NULL,
    score   INTEGER        NOT NULL
);

INSERT INTO scores (name, subject, score) VALUES
    ('Aarav', 'Math',    85),
    ('Aarav', 'Science', 90),
    ('Aarav', 'English', 78),
    ('Priya', 'Math',    92),
    ('Priya', 'Science', 88),
    ('Priya', 'English', 95),
    ('Rahul', 'Math',    70),
    ('Rahul', 'Science', 65),
    ('Rahul', 'English', 80);
