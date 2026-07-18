package com.learning.database.entity.relationship;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent (inverse) side of OneToMany bidirectional with OrderEntity.
 * Customer does NOT hold the FK — OrderEntity has `customer_id`.
 * mappedBy = "customer" refers to the field in OrderEntity.
 *
 * @JsonManagedReference("customer-order"):
 *   Serializes normally. Its counterpart @JsonBackReference("customer-order") on
 *   OrderEntity.customer is NOT serialized, breaking the Customer→Order→Customer loop.
 *
 * orphanRemoval = true: removing an order from the list deletes the order row.
 * This is different from CascadeType.REMOVE which deletes ALL orders when Customer is deleted.
 */
@Entity
@Getter
@Setter
@Table(name = "jpa_customer")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @JsonManagedReference("customer-order")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderEntity> orders = new ArrayList<>();

    /** Adds order. */
    public void addOrder(OrderEntity order) {
        orders.add(order);
        order.setCustomer(this);
    }

    /** Removes order. */
    public void removeOrder(OrderEntity order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}
