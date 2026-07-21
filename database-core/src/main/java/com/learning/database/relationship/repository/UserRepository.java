package com.learning.database.relationship.repository;

import com.learning.database.relationship.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by its email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching {@link UserEntity}, or empty if none found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Retrieves all users together with their associated address, using a
     * {@code JOIN FETCH} on {@code u.address} so that both the {@link UserEntity}
     * and its {@code OneToOne} address are loaded in a single query, avoiding
     * an N+1 select problem.
     *
     * @return the list of all {@link UserEntity} instances with their address eagerly fetched
     */
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.address")
    java.util.List<UserEntity> findAllWithAddress();
}
