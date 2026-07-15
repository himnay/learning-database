package com.learning.database.repository;

import com.learning.database.entity.converter.Priority;
import com.learning.database.entity.softdelete.ProductEntity;
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

    // Method naming
    List<ProductEntity> findByCategory(String category);
    List<ProductEntity> findByPriority(Priority priority);
    List<ProductEntity> findByPriorityAndDeletedFalse(Priority priority);

    // Paging
    Page<ProductEntity>  findByDeletedFalse(Pageable pageable);
    Slice<ProductEntity> findByCategory(String category, Pageable pageable);

    // Window / keyset scrolling (Spring Data JPA 3.1+)
    Window<ProductEntity> findTop10ByDeletedFalse(ScrollPosition position, Sort sort);

    // ── @Modifying bulk operations ────────────────────────────────────────────

    /**
     * Bulk price update — far more efficient than loading + saving each entity.
     * clearAutomatically = true clears the first-level cache post-update,
     * ensuring subsequent loads return fresh data rather than stale cached values.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ProductEntity p SET p.price = p.price * :factor WHERE p.category = :category AND p.deleted = false")
    int adjustPriceByCategory(@Param("category") String category, @Param("factor") BigDecimal factor);

    /**
     * Bulk soft-delete by category using JPQL (alternative to calling @SQLDelete per entity).
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ProductEntity p SET p.deleted = true WHERE p.category = :category")
    int softDeleteByCategory(@Param("category") String category);

    /**
     * Bulk hard-delete via native SQL (bypasses Hibernate entity lifecycle).
     * Use sparingly — no cascade, no @SQLDelete intercepted.
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM product WHERE category = :category AND deleted = true")
    int purgeDeletedByCategory(@Param("category") String category);

    // Exists query — more efficient than findBy + isEmpty()
    boolean existsByNameAndDeletedFalse(String name);
}
