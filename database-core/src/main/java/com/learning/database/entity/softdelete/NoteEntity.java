package com.learning.database.entity.softdelete;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

/**
 * Demonstrates @SoftDelete (Hibernate 6.4+) — the modern one-annotation replacement
 * for the whole @SQLDelete / @SQLRestriction / @Filter machinery.
 *
 * What it does:
 *   - deleteById() runs UPDATE note SET deleted = true WHERE id = ?
 *   - Every query automatically appends WHERE deleted = false
 *   - The `deleted` column is NOT mapped as an entity field — Hibernate owns it
 *
 * Comparison of the three soft-delete styles in this project:
 *   ProductEntity   — @SQLDelete + @FilterDef/@Filter  (toggle at runtime, most boilerplate)
 *   StockItemEntity — @SQLDelete + @SQLRestriction     (always-on, manual column)
 *   NoteEntity      — @SoftDelete                      (always-on, zero boilerplate)
 */
@Entity
@Getter
@Setter
@Table(name = "note")
@SoftDelete(columnName = "deleted")
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;
}
