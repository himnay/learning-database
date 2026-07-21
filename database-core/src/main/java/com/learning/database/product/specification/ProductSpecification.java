package com.learning.database.product.specification;

import com.learning.database.product.entity.ProductEntity;
import com.learning.database.product.service.ProductService;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * JPA Specifications — atomic predicates that can be composed with and() / or().
 *
 * Each static method returns a Specification<ProductEntity> (a Predicate factory).
 * They are combined in ProductService.searchProducts() using and()/or() chaining.
 *
 * When to use Specification over @Query:
 *   - Dynamic filters: user might send 0..N of many possible filter fields.
 *   - Reusability: same predicate used in multiple queries.
 *
 * When NOT to use Specification:
 *   - Static queries — just use method naming or @Query.
 *   - DB-specific features (window functions, CTEs) — use native @Query.
 */
public class ProductSpecification {

    /** Returns whether category. */
    public static Specification<ProductEntity> hasCategory(String category) {
        return (root, query, cb) ->
            category == null ? cb.conjunction()
                             : cb.equal(root.get("category"), category);
    }

    /** Returns the price between. */
    public static Specification<ProductEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min == null)  return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null)  return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    /** Returns the name contains. */
    public static Specification<ProductEntity> nameContains(String keyword) {
        return (root, query, cb) ->
            keyword == null ? cb.conjunction()
                            : cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<ProductEntity> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}
