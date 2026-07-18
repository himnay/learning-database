package com.learning.database.entity.relationship;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Inverse side of ManyToMany bidirectional.
 * mappedBy = "courses" refers to the field in StudentEntity.
 *
 * @JsonIgnoreProperties("courses") on the students field:
 *   When a Course is serialized, its students are included but each student's
 *   `courses` list is excluded — preventing Course→Student→Courses→... recursion.
 */
@Entity
@Getter
@Setter
@Table(name = "jpa_course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("courses")   // skip students[].courses when serializing
    private List<StudentEntity> students = new ArrayList<>();
}
