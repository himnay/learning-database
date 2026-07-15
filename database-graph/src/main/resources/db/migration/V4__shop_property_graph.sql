-- Heterogeneous property graph exercising the full CREATE PROPERTY GRAPH
-- feature set of PostgreSQL 19:
--   * several vertex tables and edge tables in one graph
--   * custom labels (table name != label name)
--   * quoted reserved-word label ("order")
--   * multiple labels on one table (customer IS ALSO a person, employee too)
--   * property renaming (employee_name AS name) so both "person" labels expose
--     the same property set — required for multi-label matching
--   * NO PROPERTIES on an edge
--   * edge properties (quantity, weight)
--   * self-referencing edge table (product -> product recommendations)

CREATE PROPERTY GRAPH myshop
    VERTEX TABLES (
        products KEY (product_no)
            LABEL product PROPERTIES (product_no, name, category, price),
        customers KEY (customer_id)
            LABEL customer PROPERTIES (customer_id, name, address)
            LABEL person   PROPERTIES (name),
        orders KEY (order_id)
            LABEL "order" PROPERTIES (order_id, ordered_when),
        employees KEY (employee_id)
            LABEL employee PROPERTIES (employee_id, department)
            LABEL person   PROPERTIES (employee_name AS name)
    )
    EDGE TABLES (
        order_items KEY (order_items_id)
            SOURCE      KEY (order_id)   REFERENCES orders (order_id)
            DESTINATION KEY (product_no) REFERENCES products (product_no)
            LABEL contains PROPERTIES (quantity),
        customer_orders KEY (customer_orders_id)
            SOURCE      KEY (customer_id) REFERENCES customers (customer_id)
            DESTINATION KEY (order_id)    REFERENCES orders (order_id)
            LABEL has_placed NO PROPERTIES,
        also_bought KEY (product_id, related_id)
            SOURCE      KEY (product_id) REFERENCES products (product_no)
            DESTINATION KEY (related_id) REFERENCES products (product_no)
            LABEL related_to PROPERTIES (weight)
    );
