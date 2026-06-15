package com.learning.database.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Target (inverse) side of OneToOne with UserEntity.
 * Address does NOT hold the FK — UserEntity does (address_id column).
 * No @OneToOne annotation here = unidirectional from User's perspective in a uni setup;
 * mappedBy = "address" is added to make it bidirectional.
 */
@Entity
@Table(name = "jpa_address")
@Getter
@Setter
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    private String zip;

    // Bidirectional back-reference — inverse side uses mappedBy
    @OneToOne(mappedBy = "address")
    private UserEntity user;
}
