-- A property graph is pure metadata on top of the relational model:
-- no extension, no copied data — GRAPH_TABLE queries are rewritten to joins.

CREATE PROPERTY GRAPH social
    VERTEX TABLES (
        person KEY (id)
            LABEL person PROPERTIES (id, name, age, city)
    )
    EDGE TABLES (
        knows KEY (a, b)
            SOURCE      KEY (a) REFERENCES person (id)
            DESTINATION KEY (b) REFERENCES person (id)
            LABEL knows PROPERTIES (since)
    );
