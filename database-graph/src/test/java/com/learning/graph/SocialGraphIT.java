package com.learning.graph;

import com.learning.graph.service.SocialGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the Flyway migrations (CREATE PROPERTY GRAPH) and real GRAPH_TABLE / MATCH queries.
 * SQL/PGQ needs PostgreSQL 19 — still beta as of Sep 2026, so the image is pinned to the
 * same beta as docker-compose.yml.
 */
@SpringBootTest
@Testcontainers
class SocialGraphIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:19beta3");

    @Autowired
    SocialGraphService social;

    @Test
    void matchListsEveryPersonVertex() {
        assertThat(social.listPersons()).containsExactly("Alice", "Bob", "Carol", "Dan", "Eve", "Frank");
    }

    @Test
    void undirectedEdgePatternMatchesBothDirections() {
        // Alice -> Bob, Alice -> Carol, Bob -> Alice, Frank -> Alice
        assertThat(social.connections("Alice")).containsExactly("Bob", "Carol", "Frank");
    }
}
