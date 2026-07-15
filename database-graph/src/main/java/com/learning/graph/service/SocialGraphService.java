package com.learning.graph.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * SQL/PGQ queries against the homogeneous "social" graph (person + knows).
 * Every method demonstrates one capability of GRAPH_TABLE in PostgreSQL 19.
 * Queries go through plain JdbcTemplate — Hibernate cannot parse GRAPH_TABLE,
 * and it does not need to: the server rewrites the pattern into joins.
 */
@Service
@RequiredArgsConstructor
public class SocialGraphService {

    private final JdbcTemplate jdbc;

    /** Simplest possible graph query — a single vertex pattern, no edges. */
    public List<String> listPersons() {
        return jdbc.queryForList("""
                SELECT name
                FROM GRAPH_TABLE (graph.social
                    MATCH (p IS person)
                    COLUMNS (p.name)
                )
                ORDER BY name
                """, String.class);
    }

    /** Single hop along a directed edge, exposing the edge property "since". */
    public List<Map<String, Object>> whoKnowsWhom() {
        return jdbc.queryForList("""
                SELECT *
                FROM GRAPH_TABLE (graph.social
                    MATCH (p IS person)-[k IS knows]->(p2 IS person)
                    COLUMNS (p.name AS who, k.since AS since, p2.name AS knows)
                )
                ORDER BY who, knows
                """);
    }

    /**
     * Two hops (friends of friends) with:
     *  - an inline WHERE inside an element pattern (a.name = ?)
     *  - a WHERE clause inside GRAPH_TABLE to drop "A knows B knows A" cycles
     */
    public List<Map<String, Object>> friendsOfFriends(String name) {
        return jdbc.queryForList("""
                SELECT *
                FROM GRAPH_TABLE (graph.social
                    MATCH (a IS person WHERE a.name = ?)-[IS knows]->
                          (b IS person)-[IS knows]->(c IS person)
                    WHERE a.id <> c.id
                    COLUMNS (a.name AS a, b.name AS via, c.name AS c)
                )
                ORDER BY a, c, via
                """, name);
    }

    /** Reversed arrow <- : walk the edge against its declared direction. */
    public List<String> whoIsKnownBy(String name) {
        return jdbc.queryForList("""
                SELECT known_by
                FROM GRAPH_TABLE (graph.social
                    MATCH (a IS person WHERE a.name = ?)<-[IS knows]-(b IS person)
                    COLUMNS (b.name AS known_by)
                )
                ORDER BY known_by
                """, String.class, name);
    }

    /** Undirected edge pattern - : matches the edge in either direction. */
    public List<String> connections(String name) {
        return jdbc.queryForList("""
                SELECT DISTINCT connected
                FROM GRAPH_TABLE (graph.social
                    MATCH (a IS person WHERE a.name = ?)-[IS knows]-(b IS person)
                    COLUMNS (b.name AS connected)
                )
                ORDER BY connected
                """, String.class, name);
    }

    /**
     * Variable-length traversal fallback. PostgreSQL 19 SQL/PGQ supports only
     * fixed-depth patterns (no +, * or {n,m} quantifiers yet), so open-ended
     * reachability still needs a recursive CTE.
     */
    public List<Map<String, Object>> reachable(String name, int maxDepth) {
        return jdbc.queryForList("""
                WITH RECURSIVE reachable AS (
                    SELECT k.b AS id, 1 AS depth
                    FROM graph.knows k
                    JOIN graph.person s ON s.id = k.a
                    WHERE s.name = ?
                    UNION ALL
                    SELECT k.b, r.depth + 1
                    FROM graph.knows k
                    JOIN reachable r ON k.a = r.id
                    WHERE r.depth < ?
                )
                SELECT p.name, MIN(r.depth) AS depth
                FROM reachable r
                JOIN graph.person p ON p.id = r.id
                GROUP BY p.name
                ORDER BY depth, name
                """, name, maxDepth);
    }

    /**
     * What happens under the hood: EXPLAIN shows plain hash joins over
     * person/knows — no dedicated graph executor nodes exist.
     */
    public List<String> explainFriendsOfFriends() {
        return jdbc.queryForList("""
                EXPLAIN SELECT *
                FROM GRAPH_TABLE (graph.social
                    MATCH (a IS person)-[IS knows]->
                          (b IS person)-[IS knows]->(c IS person)
                    WHERE a.id <> c.id
                    COLUMNS (a.name AS a, b.name AS via, c.name AS c)
                )
                ORDER BY a, c, via
                """, String.class);
    }
}
