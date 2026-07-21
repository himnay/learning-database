package com.learning.database.relationship.repository;

import com.learning.database.relationship.entity.StudentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    /**
     * Finds a student by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching {@link StudentEntity}, or empty if none found
     */
    Optional<StudentEntity> findByEmail(String email);

    /**
     * Finds a student by ID and eagerly loads all associated courses in a single query,
     * using a JOIN FETCH across the ManyToMany association to avoid the N+1 select problem.
     *
     * @param id the ID of the student to find
     * @return an {@link Optional} containing the matching {@link StudentEntity} with its courses initialized, or empty if none found
     */
    @Query("SELECT s FROM StudentEntity s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<StudentEntity> findByIdWithCourses(Long id);
}
