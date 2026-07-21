package com.learning.database.softdelete.repository;

import com.learning.database.softdelete.entity.StockItemEntity;

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

    /**
     * Finds all stock items whose stock quantity is strictly greater than the given value.
     * Derived query; implicitly restricted to non-deleted rows by {@code @SQLRestriction}.
     *
     * @param minStock the exclusive lower bound for the stock quantity
     * @return list of matching, non-deleted {@link StockItemEntity} instances
     */
    List<StockItemEntity> findByStockGreaterThan(int minStock);

    /**
     * Increments the stock quantity of the item with the given id by {@code qty} using a
     * bulk JPQL {@code UPDATE}. Marked {@code @Modifying} since it is a data-changing query,
     * and {@code @Transactional} so it executes within its own transaction. Demonstrates
     * {@code @Modifying} + {@code @Query} for bulk updates.
     *
     * @param id  the id of the stock item to update
     * @param qty the quantity to add to the current stock (may be negative to subtract)
     * @return the number of entities updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE StockItemEntity s SET s.stock = s.stock + :qty WHERE s.id = :id")
    int addStock(@Param("id") Long id, @Param("qty") int qty);
}
