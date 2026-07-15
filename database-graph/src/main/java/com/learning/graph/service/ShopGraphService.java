package com.learning.graph.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * SQL/PGQ queries against the heterogeneous "myshop" graph — several vertex
 * types (customer, "order", product, employee) and several edge types
 * (has_placed, contains, related_to) in one graph.
 */
@Service
@RequiredArgsConstructor
public class ShopGraphService {

    private final JdbcTemplate jdbc;

    /** Hop across two different vertex types via the has_placed edge. */
    public List<Map<String, Object>> customerOrders() {
        return jdbc.queryForList("""
                SELECT *
                FROM GRAPH_TABLE (graph.myshop
                    MATCH (c IS customer)-[IS has_placed]->(o IS "order")
                    COLUMNS (c.name AS customer, o.order_id AS order_id,
                             o.ordered_when AS ordered_when)
                )
                ORDER BY customer, order_id
                """);
    }

    /** Three-element path across three vertex types, with edge property. */
    public List<Map<String, Object>> orderContents() {
        return jdbc.queryForList("""
                SELECT *
                FROM GRAPH_TABLE (graph.myshop
                    MATCH (c IS customer)-[IS has_placed]->(o IS "order")
                          -[i IS contains]->(p IS product)
                    COLUMNS (c.name AS customer, o.order_id AS order_id,
                             p.name AS product, i.quantity AS quantity,
                             p.price AS price)
                )
                ORDER BY customer, order_id, product
                """);
    }

    /**
     * Multi-label matching: both customers and employees carry the "person"
     * label, so one vertex pattern returns rows from two different tables.
     */
    public List<String> allPersons() {
        return jdbc.queryForList("""
                SELECT name
                FROM GRAPH_TABLE (graph.myshop
                    MATCH (p IS person)
                    COLUMNS (p.name)
                )
                ORDER BY name
                """, String.class);
    }

    /**
     * Product recommendations: two hops over the self-referencing weighted
     * related_to edge, filtering on an edge property (weight).
     */
    public List<Map<String, Object>> recommendations(String productName, double minWeight) {
        return jdbc.queryForList("""
                SELECT DISTINCT rec_name, rec_category
                FROM GRAPH_TABLE (graph.myshop
                    MATCH (a IS product WHERE a.name = ?)
                          -[e1 IS related_to WHERE e1.weight >= ?]->(b IS product)
                          -[e2 IS related_to]->(c IS product)
                    COLUMNS (c.name AS rec_name, c.category AS rec_category)
                )
                WHERE rec_name <> ?
                """, productName, minWeight, productName);
    }

    /**
     * GRAPH_TABLE output is an ordinary row set — mix it freely with CTEs,
     * joins and aggregation of classic SQL.
     */
    public List<Map<String, Object>> topCustomersBySpend() {
        return jdbc.queryForList("""
                WITH spend AS (
                    SELECT customer, SUM(price * quantity) AS total
                    FROM GRAPH_TABLE (graph.myshop
                        MATCH (c IS customer)-[IS has_placed]->(o IS "order")
                              -[i IS contains]->(p IS product)
                        COLUMNS (c.name AS customer, i.quantity AS quantity,
                                 p.price AS price)
                    )
                    GROUP BY customer
                )
                SELECT c.name, COALESCE(s.total, 0) AS total_spend
                FROM graph.customers c
                LEFT JOIN spend s ON s.customer = c.name
                ORDER BY total_spend DESC, name
                """);
    }
}
