package com.learning.database.repository;

import com.learning.database.entity.embeddable.OrderItemEntity;
import com.learning.database.entity.embeddable.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, OrderItemId> {

    // The composite key type (OrderItemId) is passed as the ID generic type
    List<OrderItemEntity> findById_OrderId(Long orderId);
}
