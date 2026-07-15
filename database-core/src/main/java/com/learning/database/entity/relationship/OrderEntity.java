package com.learning.database.entity.relationship;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Child (owning) side of OneToMany: holds FK `customer_id`.
 * The many side of @ManyToOne is ALWAYS the owning side — never use mappedBy here.
 *
 * @JsonBackReference("customer-order"):
 *   Excluded from JSON serialization to prevent the circular reference:
 *   Customer.orders → Order.customer → Customer.orders → ...
 *
 *   Alternative approach using @JsonIgnoreProperties:
 *   @JsonIgnoreProperties("orders")  ← on the customer field
 *   This is more flexible: the customer IS serialized but without its orders collection.
 */
@Entity
@Table(name = "jpa_order")
@Getter
@Setter
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private BigDecimal amount;

    // Owning side — FK column `customer_id` is in this table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonBackReference("customer-order")
    private CustomerEntity customer;
}
