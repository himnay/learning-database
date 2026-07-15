package com.learning.database.entity.embeddable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Demonstrates @EmbeddedId — composite primary key via an embedded object.
 * The PK is (order_id, product_code) together.
 *
 * Alternative: @IdClass — same result, fields duplicated on both entity and ID class.
 * @EmbeddedId is preferred: the key is a first-class object you can pass around.
 */
@Entity
@Table(name = "jpa_order_item")
@Getter
@Setter
public class OrderItemEntity {

    @EmbeddedId
    private OrderItemId id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    // FK to jpa_order — insertable/updatable=false because order_id is already
    // managed as part of the @EmbeddedId above (avoids duplicate column mapping)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private com.learning.database.entity.relationship.OrderEntity order;
}
