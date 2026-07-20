package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * SINGLE_TABLE strategy — one table for the entire hierarchy.
 * Discriminator column `dtype` tells Hibernate which subclass a row belongs to.
 *
 * Pros:  Best query performance (no joins), simple schema.
 * Cons:  Columns specific to subclasses are nullable; cannot add NOT NULL constraints.
 */
@Entity
@Getter
@Setter
@Table(name = "vehicle")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class VehicleEntityInheritanceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;
}
