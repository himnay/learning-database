package com.learning.database.entity.relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Owning side of @OneToOne bidirectional.
 * FK column `address_id` lives in this table → UserEntity is the owning side.
 * CascadeType.ALL: saving/deleting a User also saves/deletes its Address.
 */
@Entity
@Table(name = "jpa_user")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // Owning side: @JoinColumn names the FK column in jpa_user table
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true)
    private AddressEntity address;
}
