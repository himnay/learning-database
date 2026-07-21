package com.learning.database.inheritance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @MappedSuperclass — NOT an entity, no table created for this class.
 * Fields and mappings are inherited by each concrete subclass.
 * Each subclass gets its own independent table with all parent + child columns.
 *
 * Pros:  Clean inheritance, no joins needed.
 * Cons:  Cannot query polymorphically across Computer and MobilePhone.
 *        Cannot define a FK relationship to DeviceBase.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class DeviceBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String name;
}
