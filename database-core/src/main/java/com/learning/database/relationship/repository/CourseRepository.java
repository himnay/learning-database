package com.learning.database.relationship.repository;

import com.learning.database.relationship.entity.CourseEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    /**
     * Finds a course by its exact title.
     *
     * @param title the exact course title to search for
     * @return an {@link Optional} containing the matching {@link CourseEntity}, or empty if none found
     */
    Optional<CourseEntity> findByTitle(String title);

    /**
     * Finds all courses whose title contains the given keyword, ignoring case.
     *
     * @param keyword the substring to search for within course titles
     * @return a list of matching {@link CourseEntity} instances
     */
    List<CourseEntity> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Loads a course with all of its enrolled students in one query, using a
     * {@code LEFT JOIN FETCH} on {@code c.students} to avoid a separate lazy-loading
     * query for the students collection.
     *
     * @param id the id of the course to load
     * @return an {@link Optional} containing the {@link CourseEntity} with its students eagerly
     *         fetched, or empty if no course exists with the given id
     */
    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.students WHERE c.id = :id")
    Optional<CourseEntity> findByIdWithStudents(Long id);

    /**
     * Counts the enrolled students per course, ordered by enrollment count descending.
     * <p>
     * Uses an explicit {@code LEFT JOIN} plus {@code COUNT(s)} rather than
     * {@code SIZE(c.students)} in the {@code SELECT} clause, because {@code SIZE(c.students)}
     * renders as a correlated subquery on {@code c.id}, which Postgres rejects under
     * {@code GROUP BY c.title}.
     *
     * @return a list of {@code Object[]} pairs, each containing the course title
     *         ({@link String}) and the enrolled student count ({@link Long})
     */
    @Query("SELECT c.title, COUNT(s) FROM CourseEntity c LEFT JOIN c.students s GROUP BY c.title ORDER BY COUNT(s) DESC")
    List<Object[]> findCourseEnrollmentCounts();
}
