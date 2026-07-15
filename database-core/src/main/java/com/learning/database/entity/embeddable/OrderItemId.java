package com.learning.database.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for OrderItemEntity.
 * Rules for @Embeddable composite keys:
 *   1. Must implement Serializable.
 *   2. Must override equals() and hashCode() — Lombok @EqualsAndHashCode handles this.
 *   3. Must have a no-arg constructor.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderItemId implements Serializable {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_code")
    private String productCode;
}
