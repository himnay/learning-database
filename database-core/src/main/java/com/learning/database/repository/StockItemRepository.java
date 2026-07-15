package com.learning.database.repository;

import com.learning.database.entity.softdelete.StockItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @SQLRestriction on StockItemEntity means every query here automatically
 * adds WHERE deleted = false — no session.enableFilter() required.
 * findAll() returns only non-deleted items. deleteById() triggers @SQLDelete.
 */
@Repository
public interface StockItemRepository extends JpaRepository<StockItemEntity, Long> {

    List<StockItemEntity> findByStockGreaterThan(int minStock);

    // @Modifying + @Query for bulk UPDATE (covered here as well)
    @Modifying
    @Transactional
    @Query("UPDATE StockItemEntity s SET s.stock = s.stock + :qty WHERE s.id = :id")
    int addStock(@Param("id") Long id, @Param("qty") int qty);
}
