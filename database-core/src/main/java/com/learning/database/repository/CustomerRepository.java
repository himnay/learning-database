package com.learning.database.repository;

import com.learning.database.entity.relationship.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByEmail(String email);

    @Query("SELECT c FROM CustomerEntity c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<CustomerEntity> findByIdWithOrders(Long id);
}
