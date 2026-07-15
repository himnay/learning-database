package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * TABLE_PER_CLASS strategy — abstract entity with its own table per concrete subclass.
 * Unlike @MappedSuperclass, this IS an @Entity so polymorphic queries work
 * (e.g. SELECT a FROM AnimalEntity a) — but Hibernate uses UNION ALL internally,
 * which is slow at scale. Avoid for write-heavy or large datasets.
 *
 * GenerationType.SEQUENCE with a shared sequence ensures IDs are globally unique
 * across dog and cat tables (required for polymorphic queries to work correctly).
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public abstract class AnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "animal_seq_gen")
    @SequenceGenerator(name = "animal_seq_gen", sequenceName = "animal_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;
}
