package com.learning.database.repository;

import com.learning.database.entity.relationship.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByEmail(String email);

    // JOIN FETCH for ManyToMany — loads student + all their courses in one query
    @Query("SELECT s FROM StudentEntity s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<StudentEntity> findByIdWithCourses(Long id);
}
