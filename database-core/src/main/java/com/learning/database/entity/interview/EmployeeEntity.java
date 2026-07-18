package com.learning.database.entity.interview;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.learning.database.entity.converter.Priority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Maps to existing `employees` table (V2 migration).
 *
 * Demonstrates:
 *   @ManyToOne bidirectional   — owning side (holds FK dept_id)
 *   @NamedEntityGraph          — reusable eager-loading graph for department
 *   @NamedStoredProcedureQuery — called via @Procedure in EmployeeRepository
 *   @Convert (autoApply)       — PriorityConverter auto-converts Priority enum ↔ VARCHAR
 *   @JsonBackReference         — prevents infinite JSON recursion on the "many" side
 */
@Entity
@Table(name = "employees")
@NamedEntityGraph(
    name = "Employee.withDepartment",
    attributeNodes = @NamedAttributeNode("department")
)
@NamedStoredProcedureQuery(
    name = "Employee.getTotalCount",
    procedureName = "get_total_employees",
    resultClasses = Integer.class
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
     * @Convert via PriorityConverter (autoApply = true on the converter).
     * Hibernate maps Priority enum ↔ VARCHAR "low"/"normal"/"high" in the DB.
     * No explicit @Convert needed here because autoApply = true on PriorityConverter.
     * Without the converter, JPA would use ordinal (0/1/2) by default — fragile.
     *
     * Note: `priority` column doesn't exist in the employees table (it's on product).
     * This is shown as a conceptual demo — in the project, Priority is used on ProductEntity.
     */

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
