package com.learning.database.entity.softdelete;

import com.learning.database.entity.common.AuditableBase;
import com.learning.database.entity.converter.Priority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

/**
 * Demonstrates:
 *   Soft Delete via @SQLDelete + @FilterDef + @Filter (requires manual session.enableFilter())
 *   @Convert via PriorityConverter (autoApply=true) — maps Priority enum ↔ VARCHAR
 *   Extends AuditableBase — gets @CreatedDate, @LastModifiedDate, @CreatedBy, @Version
 *
 * See StockItemEntity for the simpler @SQLRestriction approach (always-on filter).
 *
 * Soft Delete Pattern:
 *   @SQLDelete intercepts repo.deleteById(id) → runs UPDATE SET deleted=true instead
 *   @Filter    adds WHERE deleted=? only when explicitly enabled per Session
 *
 * Two Jackson serialization strategies are NOT needed here (no bidirectional relationship),
 * but see CustomerEntity / OrderEntity for @JsonManagedReference / @JsonBackReference example.
 */
@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET deleted = true WHERE id = ? AND version = ?")
@FilterDef(
    name = "deletedProductFilter",
    parameters = @ParamDef(name = "isDeleted", type = Boolean.class)
)
@Filter(name = "deletedProductFilter", condition = "deleted = :isDeleted")
@Getter
@Setter
public class ProductEntity extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    /**
     * @Convert via PriorityConverter (autoApply = true) — no explicit @Convert needed.
     * PriorityConverter maps:  Priority.HIGH ↔ "high"  (stored as VARCHAR in DB)
     *
     * If autoApply were false, you'd write:
     *   @Convert(converter = PriorityConverter.class)
     *   private Priority priority;
     */
    @Column(nullable = false)
    private Priority priority = Priority.NORMAL;

    @Column(nullable = false)
    private boolean deleted = false;
}
