package com.learning.database.repository;

import com.learning.database.entity.relationship.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    Optional<CourseEntity> findByTitle(String title);

    List<CourseEntity> findByTitleContainingIgnoreCase(String keyword);

    // Load course with all enrolled students in one query
    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.students WHERE c.id = :id")
    Optional<CourseEntity> findByIdWithStudents(Long id);

    // Count enrolled students per course.
    // NOTE: SIZE(c.students) in the SELECT renders as a correlated subquery on c.id,
    // which Postgres rejects under GROUP BY c.title — use an explicit JOIN + COUNT instead.
    @Query("SELECT c.title, COUNT(s) FROM CourseEntity c LEFT JOIN c.students s GROUP BY c.title ORDER BY COUNT(s) DESC")
    List<Object[]> findCourseEnrollmentCounts();
}
