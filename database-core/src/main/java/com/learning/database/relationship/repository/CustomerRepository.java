package com.learning.database.relationship.repository;

import com.learning.database.relationship.entity.CustomerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    /**
     * Derived query that finds a customer by its unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching {@link CustomerEntity}, or empty if none found
     */
    Optional<CustomerEntity> findByEmail(String email);

    /**
     * Custom JPQL query that fetches a customer by id and eagerly loads its associated
     * orders via a {@code LEFT JOIN FETCH}, avoiding a separate lazy-loading query for
     * the {@code orders} collection.
     *
     * @param id the identifier of the customer to fetch
     * @return an {@link Optional} containing the {@link CustomerEntity} with its orders
     *         initialized, or empty if no customer matches the given id
     */
    @Query("SELECT c FROM CustomerEntity c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<CustomerEntity> findByIdWithOrders(Long id);
}
