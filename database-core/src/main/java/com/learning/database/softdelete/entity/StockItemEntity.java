package com.learning.database.softdelete.entity;

import com.learning.database.product.entity.ProductEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Demonstrates @SQLRestriction — the Hibernate 6.3+ replacement for the deprecated @Where.
 *
 * Comparison with ProductEntity (@Filter approach):
 *
 * │ Feature               │ @Filter (ProductEntity)           │ @SQLRestriction (StockItemEntity)  │
 * │ Activation            │ Manual — session.enableFilter()   │ Automatic — always active          │
 * │ Toggle at runtime     │ Yes                               │ No                                 │
 * │ See deleted records   │ Yes (enable filter with true)     │ No (always hidden)                 │
 * │ Simplicity            │ More boilerplate                  │ Simpler                            │
 *
 * When to use @SQLRestriction:
 *   You never want to query deleted rows (most common use case).
 *
 * When to use @Filter:
 *   You sometimes need to query deleted rows too (e.g. admin views that show all).
 */
@Entity
@Getter
@Setter
@Table(name = "stock_item")
@SQLDelete(sql = "UPDATE stock_item SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")   // appended to ALL queries on this entity automatically
public class StockItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean deleted = false;
}
