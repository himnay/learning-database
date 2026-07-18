package com.learning.database.entity.interview;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the existing `departments` table (V1 migration).
 * Inverse side of the bidirectional Department <-> Employee (OneToMany/ManyToOne).
 *
 * Demonstrates:
 *   @Formula          — read-only computed field from SQL (no DB column needed)
 *   @BatchSize        — N+1 mitigation: loads employees in batches instead of 1-by-1
 *   @Fetch(SUBSELECT) — alternative N+1 fix (commented out — can't use both at once)
 *   @JsonManagedReference — prevents infinite JSON recursion on the "one" side
 */
@Entity
@Getter
@Setter
@Table(name = "departments")
public class DepartmentEntity {

    @Id
    @Column(name = "dept_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deptId;

    @Column(name = "dept_name", nullable = false)
    private String deptName;

    /**
     * @Formula — computed at SELECT time from raw SQL. Read-only (no setter, no DB column).
     * Hibernate appends a subquery to the SELECT list automatically.
     *
     * Note: column names in @Formula are DB column names, not Java field names.
     */
    @Formula("(SELECT COUNT(*) FROM employees e WHERE e.dept_id = dept_id)")
    private Integer employeeCount;

    /**
     * @BatchSize(size = 20): When N departments are loaded and their employees are accessed,
     * Hibernate issues ceil(N/20) queries using IN clauses instead of N separate queries.
     *
     * Example with 4 departments:
     *   Without @BatchSize: 4 queries  (SELECT WHERE dept_id=1), (=2), (=3), (=4)
     *   With @BatchSize(20): 1 query   (SELECT WHERE dept_id IN (1, 2, 3, 4))
     *
     * Alternative (uncomment to switch):
     * @Fetch(FetchMode.SUBSELECT) — loads all at once with a single subquery:
     *   SELECT * FROM employees WHERE dept_id IN (SELECT dept_id FROM departments WHERE ...)
     */
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @Fetch(FetchMode.SUBSELECT)   // ← uncomment to try the SUBSELECT approach instead
    @JsonManagedReference("dept-employee")   // serialized normally; breaks the circular JSON reference
    private List<EmployeeEntity> employees = new ArrayList<>();

    public void addEmployee(EmployeeEntity employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    public void removeEmployee(EmployeeEntity employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }
}
