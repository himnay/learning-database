package com.learning.database.employee.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Maps to existing `employees` table (V2 migration; `version` column added in V14).
 *
 * Demonstrates:
 *   @ManyToOne bidirectional   — owning side (holds FK dept_id)
 *   @NamedEntityGraph          — reusable eager-loading graph for department
 *   @NamedStoredProcedureQuery — called via @Procedure in EmployeeRepository
 *   @Version                   — optimistic locking (needed by @Lock(OPTIMISTIC) in EmployeeRepository)
 *   @JsonBackReference         — prevents infinite JSON recursion on the "many" side
 */
@Entity
@Table(name = "employees")
@NamedEntityGraph(
    name = "Employee.withDepartment",
    attributeNodes = @NamedAttributeNode("department")
)
// get_total_employees is a PostgreSQL FUNCTION, not a PROCEDURE — Hibernate 6 would
// otherwise emit `CALL get_total_employees()` and Postgres rejects it (SQLState 42809).
// The org.hibernate.callableFunction hint makes Hibernate treat it as a function call.
@NamedStoredProcedureQuery(
    name = "Employee.getTotalCount",
    procedureName = "get_total_employees",
    hints = @QueryHint(name = "org.hibernate.callableFunction", value = "true")
)
@Getter
@Setter
public class EmployeeEntity {

    @Id
    @Column(name = "emp_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer empId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "salary", nullable = false)
    private BigDecimal salary;

    @Column(name = "col_a")
    private String colA;

    @Column(name = "col_b")
    private String colB;

    /**
     * Optimistic-locking version (V14). Hibernate adds {@code AND version = ?} to every
     * UPDATE/DELETE it issues for this entity and bumps the value; a zero row count means a
     * concurrent writer got there first ({@code OptimisticLockException}).
     * {@code @Lock(OPTIMISTIC)} additionally re-reads the version at commit, even for rows
     * that were only read. JPQL bulk updates ({@code @Modifying}) bypass it.
     */
    @Version
    private Long version;

    /**
     * Owning side: Employee table holds FK column `dept_id`.
     * LAZY avoids loading Department with every Employee fetch (N+1 risk).
     *
     * @JsonBackReference: when an Employee is serialized, the `department` field is
     * excluded from JSON output. The DepartmentEntity.employees list (@JsonManagedReference)
     * IS included. This breaks the circular reference:
     *   Department → employees[] → each employee.department → Department → ...
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("dept-employee")
    @JoinColumn(name = "dept_id", referencedColumnName = "dept_id")
    private DepartmentEntity department;
}
