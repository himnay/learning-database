package com.learning.database.embeddable.repository;

import com.learning.database.embeddable.entity.OrderItemEntity;
import com.learning.database.embeddable.entity.OrderItemId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, OrderItemId> {

    /**
     * Finds all order items whose embedded composite id ({@link OrderItemId}) has the given
     * order id. The composite key type ({@code OrderItemId}) is passed as the {@code ID} generic
     * type of {@link JpaRepository}, and this derived query navigates into its {@code orderId}
     * field.
     *
     * @param orderId the order id to match within the embedded {@link OrderItemId}
     * @return the list of order items belonging to the given order id
     */
    List<OrderItemEntity> findById_OrderId(Long orderId);
}
