package com.learning.database;

import com.learning.database.product.entity.ProductEntity;
import com.learning.database.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Window / ScrollPosition demos against the seeded products (V8): the active ones, by price,
 * are PostgreSQL Guide (40.00, id 4), Spring Boot in Action (45.00, id 1), Effective Java (50.00, id 2).
 */
@SpringBootTest
@Testcontainers
class ProductScrollIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:19beta3");

    @Autowired
    ProductService products;

    @Test
    void firstOffsetWindowStartsAtTheFirstRow() {
        // ScrollPosition.offset(0) would mean "after row 0" (OFFSET 1) and silently drop PostgreSQL Guide
        Window<ProductEntity> first = products.getProductsWindowOffset(ScrollPosition.offset());

        assertThat(first.getContent()).extracting(ProductEntity::getName)
                .containsExactly("PostgreSQL Guide", "Spring Boot in Action", "Effective Java");
    }

    @Test
    void keysetWindowHonoursTheCursorEvenAfterAnOffsetScroll() {
        // An offset call used to cache "ORDER BY price OFFSET ?" for the shared repository method,
        // and the keyset call below then ran without its WHERE cursor.
        products.getProductsWindowOffset(ScrollPosition.offset());

        Window<ProductEntity> next = products.getProductsWindowKeyset(
                ScrollPosition.forward(Map.of("price", new BigDecimal("40.00"), "id", 4L)));

        assertThat(next.getContent()).extracting(ProductEntity::getName)
                .containsExactly("Spring Boot in Action", "Effective Java");
    }
}
