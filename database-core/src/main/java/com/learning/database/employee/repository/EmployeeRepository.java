package com.learning.database.employee.repository;

import com.learning.database.audit.entity.AuditableBase;
import com.learning.database.employee.entity.EmployeeEntity;
import com.learning.database.employee.projection.EmployeeNameView;
import com.learning.database.employee.projection.EmployeeSummaryDTO;

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

    /**
     * Derived query traversing the {@code department} association by its {@code deptId}.
     *
     * @param deptId the department id to filter by
     * @return employees belonging to the given department
     */
    List<EmployeeEntity> findByDepartment_DeptId(Integer deptId);

    /**
     * Derived query — employees with salary strictly greater than the given amount.
     *
     * @param salary the salary threshold (exclusive)
     * @return matching employees
     */
    List<EmployeeEntity> findBySalaryGreaterThan(BigDecimal salary);

    /**
     * Derived query — case-insensitive substring match on first name.
     *
     * @param keyword substring to search for
     * @return matching employees
     */
    List<EmployeeEntity> findByFirstNameContainingIgnoreCase(String keyword);

    /**
     * Derived query — the single highest-paid employee.
     *
     * @return the top earner, if any employees exist
     */
    Optional<EmployeeEntity> findFirstByOrderBySalaryDesc();

    /**
     * Derived query — top 5 highest earners above the given salary, descending.
     *
     * @param salary the salary threshold (exclusive)
     * @return up to 5 matching employees, ordered by salary descending
     */
    List<EmployeeEntity> findTop5BySalaryGreaterThanOrderBySalaryDesc(BigDecimal salary);

    /**
     * Derived existence check — avoids loading the entity just to test for presence.
     *
     * @param email the email to check
     * @return {@code true} if an employee with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * JPQL — employees with salary between {@code min} and {@code max} (inclusive), highest first.
     *
     * @param min lower salary bound (inclusive)
     * @param max upper salary bound (inclusive)
     * @return matching employees ordered by salary descending
     */
    @Query("SELECT e FROM EmployeeEntity e WHERE e.salary BETWEEN :min AND :max ORDER BY e.salary DESC")
    List<EmployeeEntity> findBySalaryRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    /**
     * JPQL with JOIN FETCH — loads the department eagerly in the same query, avoiding N+1.
     *
     * @param deptId the department id to filter by
     * @return employees in the department, with department already initialized
     */
    @Query("SELECT e FROM EmployeeEntity e JOIN FETCH e.department WHERE e.department.deptId = :deptId")
    List<EmployeeEntity> findByDeptIdWithDepartment(@Param("deptId") Integer deptId);

    /**
     * @EntityGraph references the @NamedEntityGraph declared on EmployeeEntity.
     * Hibernate adds a LEFT OUTER JOIN on the `departments` table automatically.
     * Advantage over JOIN FETCH: no need to write custom JPQL — method naming still works.
     */
    @EntityGraph("Employee.withDepartment")
    List<EmployeeEntity> findByFirstNameContaining(String name);

    /**
     * Overrides {@link JpaRepository#findAll(Pageable)} to eagerly fetch the department
     * via {@code @EntityGraph}, avoiding N+1 when paging.
     *
     * @param pageable paging and sorting parameters
     * @return a page of employees with department pre-fetched
     */
    @EntityGraph(attributePaths = {"department"})
    Page<EmployeeEntity> findAll(Pageable pageable);

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


    /**
     * @Modifying marks a non-SELECT @Query (UPDATE, DELETE, INSERT via native).
     * @Transactional is required — Spring Data repositories are not transactional by default
     * for write operations triggered via @Query.
     *
     * clearAutomatically = true: clears the first-level cache after the bulk operation so
     * subsequent findById calls see the updated data, not stale cached values.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE EmployeeEntity e SET e.salary = e.salary * :multiplier WHERE e.department.deptId = :deptId")
    int updateSalaryByDepartment(@Param("deptId") Integer deptId, @Param("multiplier") BigDecimal multiplier);

    /**
     * Bulk UPDATE of a single employee's email, bypassing the persistence context.
     *
     * @param id    the employee id to update
     * @param email the new email address
     * @return number of rows affected (0 or 1)
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE EmployeeEntity e SET e.email = :email WHERE e.empId = :id")
    int updateEmail(@Param("id") Integer id, @Param("email") String email);

    /**
     * Native hard DELETE — permanently removes the row, bypassing any soft-delete mechanism.
     *
     * @param empId the employee id to delete
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM employees WHERE emp_id = :empId")
    void hardDeleteById(@Param("empId") Integer empId);

    /**
     * Native SQL query — employees matching the given last name exactly.
     *
     * @param lastName the last name to search for
     * @return matching employees
     */
    @Query(nativeQuery = true, value = "SELECT * FROM employees WHERE last_name = :lastName")
    List<EmployeeEntity> findByLastName(@Param("lastName") String lastName);

    /**
     * Interface projection — first/last name only, avoiding a full entity load.
     *
     * @return name-only views of every employee
     */
    @Query("SELECT e.firstName AS firstName, e.lastName AS lastName FROM EmployeeEntity e")
    List<EmployeeNameView> findEmployeeNames();

    /**
     * DTO projection via JPQL constructor expression — maps directly into {@link EmployeeSummaryDTO}.
     *
     * @return summary DTOs for every employee
     */
    @Query("SELECT new com.learning.database.employee.projection.EmployeeSummaryDTO(e.firstName, e.lastName, e.salary) FROM EmployeeEntity e")
    List<EmployeeSummaryDTO> findEmployeeSummaries();

    /**
     * Tuple projection — alias-addressable result rows, useful for ad hoc column sets.
     *
     * @return one {@link Tuple} per employee with {@code id}, {@code firstName}, {@code salary}
     */
    @Query("SELECT e.empId AS id, e.firstName AS firstName, e.salary AS salary FROM EmployeeEntity e")
    List<Tuple> findEmployeeTuples();

    /**
     * Paged variant of {@link #findByDepartment_DeptId(Integer)}.
     *
     * @param deptId   the department id to filter by
     * @param pageable paging and sorting parameters
     * @return a page of matching employees
     */
    Page<EmployeeEntity> findByDepartment_DeptId(Integer deptId, Pageable pageable);

    /**
     * Derived query returning a {@link Slice} — no COUNT query, only knows if a next page exists.
     *
     * @param firstName exact first name to match
     * @param pageable  paging and sorting parameters
     * @return a slice of matching employees
     */
    Slice<EmployeeEntity> findByFirstName(String firstName, Pageable pageable);

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

    /**
     * Native SQL — demonstrates window functions (ROW_NUMBER, RANK, DENSE_RANK, NTILE, LAG, LEAD)
     * applied over all employees ordered by salary descending.
     *
     * @return one {@code Object[]} per employee: firstName, salary, rowNum, rank, denseRank,
     *         quartile, prevEmp, nextEmp
     */
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

    /**
     * Calls the {@code Employee.getTotalCount} named stored procedure.
     *
     * @return total number of employees
     */
    @Procedure(name = "Employee.getTotalCount")
    Integer getTotalEmployeeCount();
}
