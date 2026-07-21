package com.learning.database.relationship.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Owning side of ManyToMany bidirectional.
 *
 * @JsonIgnoreProperties("students") on the courses field:
 *   When a Student is serialized, its courses are included BUT each course's
 *   `students` list is excluded — breaking the Student→Course→Students→... loop.
 *
 *   This is the preferred alternative to @JsonManagedReference/@JsonBackReference
 *   for ManyToMany, because both sides need to be serializable independently.
 *   @JsonBackReference would make CourseEntity's students field invisible everywhere,
 *   which is too aggressive for ManyToMany.
 */
@Entity
@Getter
@Setter
@Table(name = "jpa_student")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "jpa_student_course",
        joinColumns        = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnoreProperties("students")  // when serializing, skip courses[].students to break the loop
    private List<CourseEntity> courses = new ArrayList<>();

    /** Handles enroll. */
    public void enroll(CourseEntity course) {
        courses.add(course);
        course.getStudents().add(this);
    }

    /** Handles drop. */
    public void drop(CourseEntity course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }
}
