package com.learning.database.product.repository;

import com.learning.database.product.converter.Priority;
import com.learning.database.product.entity.ProductEntity;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * JpaSpecificationExecutor enables Specification-based dynamic queries.
 * QueryByExampleExecutor is inherited from JpaRepository.
 */
@Repository
public interface ProductRepository
        extends JpaRepository<ProductEntity, Long>,
                JpaSpecificationExecutor<ProductEntity> {

    /**
     * Finds all products belonging to the given category.
     *
     * @param category the category to filter by
     * @return list of products matching the category
     */
    List<ProductEntity> findByCategory(String category);

    /**
     * Finds all products with the given priority.
     *
     * @param priority the priority to filter by
     * @return list of products matching the priority
     */
    List<ProductEntity> findByPriority(Priority priority);

    /**
     * Finds all non-deleted products with the given priority.
     *
     * @param priority the priority to filter by
     * @return list of non-deleted products matching the priority
     */
    List<ProductEntity> findByPriorityAndDeletedFalse(Priority priority);

    /**
     * Finds non-deleted products, paginated.
     *
     * @param pageable paging and sorting information
     * @return page of non-deleted products
     */
    Page<ProductEntity>  findByDeletedFalse(Pageable pageable);

    /**
     * Finds products by category as a scrollable slice.
     *
     * @param category the category to filter by
     * @param pageable paging and sorting information
     * @return slice of products matching the category
     */
    Slice<ProductEntity> findByCategory(String category, Pageable pageable);

    /**
     * Finds the top 10 non-deleted products using keyset-based scrolling
     * (Spring Data JPA 3.1+ Window/ScrollPosition API).
     *
     * @param position the scroll position (keyset) to continue scrolling from
     * @param sort the sort order used for keyset scrolling
     * @return a window of up to 10 non-deleted products
     */
    Window<ProductEntity> findTop10ByDeletedFalse(ScrollPosition position, Sort sort);

    /**
     * Bulk price update — far more efficient than loading + saving each entity.
     * clearAutomatically = true clears the first-level cache post-update,
     * ensuring subsequent loads return fresh data rather than stale cached values.
     *
     * @param category the category whose products' prices are adjusted
     * @param factor the multiplier applied to the current price
     * @return the number of rows updated
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductEntity p SET p.price = p.price * :factor WHERE p.category = :category AND p.deleted = false")
    int adjustPriceByCategory(@Param("category") String category, @Param("factor") BigDecimal factor);

    /**
     * Bulk soft-delete by category using JPQL (alternative to calling @SQLDelete per entity).
     *
     * @param category the category whose products are marked deleted
     * @return the number of rows updated
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductEntity p SET p.deleted = true WHERE p.category = :category")
    int softDeleteByCategory(@Param("category") String category);

    /**
     * Bulk hard-delete via native SQL (bypasses Hibernate entity lifecycle).
     * Use sparingly — no cascade, no @SQLDelete intercepted.
     *
     * @param category the category whose already-deleted products are purged
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM product WHERE category = :category AND deleted = true")
    int purgeDeletedByCategory(@Param("category") String category);

    /**
     * Checks whether a non-deleted product with the given name exists.
     * More efficient than findBy + isEmpty() since it issues an EXISTS query.
     *
     * @param name the product name to check
     * @return true if a matching non-deleted product exists, false otherwise
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * Native upsert — INSERT, or UPDATE if the row already exists.
     * Requires a unique constraint on the conflict target (product_name_unique, V11).
     * JPA has no portable upsert; save() is select-then-insert/update (2 round trips,
     * race-prone). ON CONFLICT is atomic and a single statement.
     *
     * @param name the product name (conflict target)
     * @param price the price to insert, or to set on conflicting row
     * @param category the category to insert, or to set on conflicting row
     * @return the number of rows affected (always 1)
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
            INSERT INTO product (name, price, category, priority, deleted, version)
            VALUES (:name, :price, :category, 'normal', false, 0)
            ON CONFLICT (name) DO UPDATE SET
                price    = EXCLUDED.price,
                category = EXCLUDED.category,
                version  = product.version + 1
            """)
    int upsertProduct(@Param("name") String name,
                      @Param("price") BigDecimal price,
                      @Param("category") String category);
}
