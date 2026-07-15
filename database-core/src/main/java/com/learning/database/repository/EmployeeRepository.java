package com.learning.database.repository;

import com.learning.database.entity.interview.EmployeeEntity;
import com.learning.database.projection.EmployeeNameView;
import com.learning.database.projection.EmployeeSummaryDTO;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository
        extends JpaRepository<EmployeeEntity, Integer>,
                JpaSpecificationExecutor<EmployeeEntity> {

    // ── Method Naming Queries ─────────────────────────────────────────────────

    List<EmployeeEntity> findByDepartment_DeptId(Integer deptId);
    List<EmployeeEntity> findBySalaryGreaterThan(BigDecimal salary);
    List<EmployeeEntity> findByFirstNameContainingIgnoreCase(String keyword);
    Optional<EmployeeEntity> findFirstByOrderBySalaryDesc();
    List<EmployeeEntity> findTop5BySalaryGreaterThanOrderBySalaryDesc(BigDecimal salary);
    boolean existsByEmail(String email);

    // ── JPQL Queries ──────────────────────────────────────────────────────────

    @Query("SELECT e FROM EmployeeEntity e WHERE e.salary BETWEEN :min AND :max ORDER BY e.salary DESC")
    List<EmployeeEntity> findBySalaryRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    // JOIN FETCH — solves N+1 for @ManyToOne in a single SQL JOIN
    @Query("SELECT e FROM EmployeeEntity e JOIN FETCH e.department WHERE e.department.deptId = :deptId")
    List<EmployeeEntity> findByDeptIdWithDepartment(@Param("deptId") Integer deptId);

    // ── @EntityGraph — alternative to JOIN FETCH, works on derived queries ───

    /**
     * @EntityGraph references the @NamedEntityGraph declared on EmployeeEntity.
     * Hibernate adds a LEFT OUTER JOIN on the `departments` table automatically.
     * Advantage over JOIN FETCH: no need to write custom JPQL — method naming still works.
     */
    @EntityGraph("Employee.withDepartment")
    List<EmployeeEntity> findByFirstNameContaining(String name);

    // Ad-hoc EntityGraph (no @NamedEntityGraph needed on the entity)
    @EntityGraph(attributePaths = {"department"})
    Page<EmployeeEntity> findAll(Pageable pageable);

    // ── @Lock — Pessimistic Locking ───────────────────────────────────────────

    /**
     * PESSIMISTIC_WRITE → SELECT ... FOR UPDATE
     * Locks the row until the transaction commits.
     * Use when two concurrent transactions might both read then update the same row.
     * Compare with @Version (optimistic locking) — optimistic fails at commit time,
     * pessimistic fails at read time (waits or throws immediately).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeEntity e WHERE e.empId = :id")
    Optional<EmployeeEntity> findByIdForUpdate(@Param("id") Integer id);

    /**
     * PESSIMISTIC_READ → SELECT ... FOR SHARE
     * Multiple readers can hold this lock simultaneously. Writers are blocked.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT e FROM EmployeeEntity e WHERE e.empId = :id")
    Optional<EmployeeEntity> findByIdForShare(@Param("id") Integer id);

    /**
     * OPTIMISTIC — uses @Version for conflict detection at commit time.
     * Hibernate checks that the version column hasn't changed since the entity was read.
     * Throws OptimisticLockException if another transaction updated the row first.
     * (AuditableBase has @Version — only relevant if EmployeeEntity extends it)
     */
    @Lock(LockModeType.OPTIMISTIC)
    Optional<EmployeeEntity> findByEmail(String email);

    // ── @Modifying — Bulk UPDATE / DELETE ────────────────────────────────────

    /**
     * @Modifying marks a non-SELECT @Query (UPDATE, DELETE, INSERT via native).
     * @Transactional is required — Spring Data repositories are not transactional by default
     * for write operations triggered via @Query.
     *
     * clearAutomatically = true: clears the first-level cache after the bulk operation so
     * subsequent findById calls see the updated data, not stale cached values.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE EmployeeEntity e SET e.salary = e.salary * :multiplier WHERE e.department.deptId = :deptId")
    int updateSalaryByDepartment(@Param("deptId") Integer deptId, @Param("multiplier") BigDecimal multiplier);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE EmployeeEntity e SET e.email = :email WHERE e.empId = :id")
    int updateEmail(@Param("id") Integer id, @Param("email") String email);

    // Hard delete via native SQL (bypasses @SQLDelete if one were present)
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM employees WHERE emp_id = :empId")
    void hardDeleteById(@Param("empId") Integer empId);

    // ── Native SQL (@Query nativeQuery) ──────────────────────────────────────

    @Query(nativeQuery = true, value = "SELECT * FROM employees WHERE last_name = :lastName")
    List<EmployeeEntity> findByLastName(@Param("lastName") String lastName);

    // ── Projections ───────────────────────────────────────────────────────────

    @Query("SELECT e.firstName AS firstName, e.lastName AS lastName FROM EmployeeEntity e")
    List<EmployeeNameView> findEmployeeNames();

    @Query("SELECT new com.learning.database.projection.EmployeeSummaryDTO(e.firstName, e.lastName, e.salary) FROM EmployeeEntity e")
    List<EmployeeSummaryDTO> findEmployeeSummaries();

    @Query("SELECT e.empId AS id, e.firstName AS firstName, e.salary AS salary FROM EmployeeEntity e")
    List<Tuple> findEmployeeTuples();

    // ── Paging / Sorting / Slicing ────────────────────────────────────────────

    Page<EmployeeEntity> findByDepartment_DeptId(Integer deptId, Pageable pageable);
    Slice<EmployeeEntity> findByFirstName(String firstName, Pageable pageable);

    // ── Window / ScrollPosition API (Spring Data JPA 3.1+) ───────────────────

    /**
     * Keyset-based (cursor) pagination — much more efficient than OFFSET for large tables.
     *
     * OFFSET pagination: SELECT * FROM employees ORDER BY salary DESC OFFSET 1000 LIMIT 10
     *   → Database must scan + skip 1000 rows on every page. O(N) cost per page.
     *
     * Keyset pagination: SELECT * FROM employees WHERE salary < :lastSeenSalary ORDER BY salary DESC LIMIT 10
     *   → Uses the index on salary directly. O(1) cost regardless of page depth.
     *
     * Usage:
     *   Window<EmployeeEntity> first  = repo.findTop10By(ScrollPosition.keyset(), sort);
     *   Window<EmployeeEntity> second = repo.findTop10By(first.positionAt(9), sort);
     */
    Window<EmployeeEntity> findTop10By(ScrollPosition position, Sort sort);

    // ── Window Functions (native SQL — FAQ Q3, Q4, Q8, Q9) ──────────────────

    @Query(nativeQuery = true, value = """
            SELECT first_name, salary,
                   ROW_NUMBER()  OVER (ORDER BY salary DESC) AS row_num,
                   RANK()        OVER (ORDER BY salary DESC) AS rnk,
                   DENSE_RANK()  OVER (ORDER BY salary DESC) AS dense_rnk,
                   NTILE(4)      OVER (ORDER BY salary DESC) AS quartile,
                   LAG(first_name)  OVER (ORDER BY salary DESC) AS prev_emp,
                   LEAD(first_name) OVER (ORDER BY salary DESC) AS next_emp
            FROM employees
            ORDER BY salary DESC
            """)
    List<Object[]> findWithWindowFunctions();

    // ── Stored Procedure ──────────────────────────────────────────────────────

    @Procedure(name = "Employee.getTotalCount")
    Integer getTotalEmployeeCount();
}
