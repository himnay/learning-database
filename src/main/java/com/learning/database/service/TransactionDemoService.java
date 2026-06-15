package com.learning.database.service;

import com.learning.database.entity.interview.EmployeeEntity;
import com.learning.database.repository.DepartmentRepository;
import com.learning.database.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates every important @Transactional attribute:
 *   - propagation (REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS, NOT_SUPPORTED, NEVER, MANDATORY)
 *   - isolation  (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
 *   - rollbackFor / noRollbackFor
 *   - timeout
 *   - readOnly
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionDemoService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    // ── Propagation ──────────────────────────────────────────────────────────

    /**
     * REQUIRED (default): Joins the caller's transaction if one exists.
     * If no transaction exists, creates a new one.
     * Most common — use this for almost all service methods.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredExample() {
        employeeRepository.findAll(); // runs in caller's tx or starts its own
    }

    /**
     * REQUIRES_NEW: Always suspends any existing transaction and starts a brand new one.
     * Commits/rolls back independently of the caller.
     *
     * Real-world use case: Audit logging — you want the audit entry saved even
     * if the main business transaction rolls back.
     *
     * Without REQUIRES_NEW: if the outer tx rolls back, the audit log is also lost.
     * With REQUIRES_NEW: the audit log commits in its own tx regardless of outer result.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewExample(String action) {
        // This commits even if the caller's transaction rolls back
        log.info("Audit log saved independently: {}", action);
    }

    /**
     * MANDATORY: Must run inside an existing transaction. Throws if there is none.
     * Use for DAO/helper methods that should never run outside a service transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<EmployeeEntity> mandatoryExample() {
        return employeeRepository.findAll(); // will throw if called without active transaction
    }

    /**
     * SUPPORTS: Runs in the caller's transaction if one exists; runs non-transactionally if not.
     * Use for read-only helpers that can work either way.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<EmployeeEntity> supportsExample() {
        return employeeRepository.findAll();
    }

    /**
     * NOT_SUPPORTED: Always suspends any active transaction and runs without one.
     * Use for operations that must not be transactional (e.g., calling a legacy system).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupportedExample() {
        log.info("Running without a transaction");
    }

    /**
     * NEVER: Must NOT have an active transaction. Throws if one exists.
     */
    @Transactional(propagation = Propagation.NEVER)
    public void neverExample() {
        log.info("Running — will throw if a transaction is active");
    }

    /**
     * NESTED: Creates a savepoint inside the existing transaction.
     * If this method fails, it rolls back only to the savepoint — the outer transaction can continue.
     * If the outer transaction fails, nested is rolled back too.
     *
     * Requires JDBC savepoint support (most RDBMS support it; JPA-only setups may not).
     */
    @Transactional(propagation = Propagation.NESTED)
    public void nestedExample() {
        // savepoint created here; rollback goes to this point, not the start of the outer tx
        employeeRepository.findBySalaryGreaterThan(new BigDecimal("50000"));
    }

    // ── Isolation Levels ────────────────────────────────────────────────────

    /**
     * READ_UNCOMMITTED: Can read uncommitted data from other transactions ("dirty reads").
     * Fastest but least safe. Almost never used in practice.
     *
     * Dirty read example: Tx B reads Tx A's updated salary before Tx A commits.
     * If Tx A rolls back, Tx B has read data that never existed.
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<EmployeeEntity> readUncommittedExample() {
        return employeeRepository.findAll();
    }

    /**
     * READ_COMMITTED (PostgreSQL default): Only reads committed data.
     * Prevents dirty reads. Can still have non-repeatable reads.
     *
     * Non-repeatable read: you read the same row twice in a transaction and get different
     * values because another transaction committed a change between your two reads.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<EmployeeEntity> readCommittedExample() {
        return employeeRepository.findAll();
    }

    /**
     * REPEATABLE_READ: Guarantees same data if you read the same row multiple times.
     * Prevents dirty + non-repeatable reads. Can still have phantom reads.
     *
     * Phantom read: a query for "salary > 80000" returns 3 rows, then another
     * transaction inserts a new high earner, and a re-run of the same query returns 4 rows.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public List<EmployeeEntity> repeatableReadExample() {
        return employeeRepository.findAll();
    }

    /**
     * SERIALIZABLE: Full isolation — transactions appear to execute one after another.
     * Prevents all anomalies. Slowest — uses range locks or conflict detection.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void serializableExample() {
        employeeRepository.findAll();
    }

    // ── rollbackFor / noRollbackFor ──────────────────────────────────────────

    /**
     * By default, Spring rolls back only on RuntimeException (unchecked) and Error.
     * Checked exceptions do NOT trigger rollback by default.
     *
     * rollbackFor = Exception.class makes ALL exceptions (checked + unchecked) trigger rollback.
     */
    @Transactional(rollbackFor = Exception.class)
    public void rollbackForCheckedExample() throws Exception {
        employeeRepository.findAll();
        // if this throws a checked Exception, the transaction WILL rollback
    }

    /**
     * noRollbackFor: Commit the transaction even if these specific exceptions are thrown.
     * Use when a specific exception is expected and should not undo the work.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void noRollbackForExample(Integer empId) {
        employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + empId));
        // Even if IllegalArgumentException is thrown, the transaction commits
    }

    // ── timeout & readOnly ───────────────────────────────────────────────────

    /**
     * timeout = 3: Roll back the transaction if it runs longer than 3 seconds.
     * Prevents long-running transactions from holding locks.
     */
    @Transactional(timeout = 3)
    public List<EmployeeEntity> timeoutExample() {
        return employeeRepository.findAll();
    }

    /**
     * readOnly = true:
     *   - Hibernate skips dirty checking (no need to track changes → faster).
     *   - Some JDBC drivers route reads to replicas.
     *   - Clearly signals intent — any accidental writes will be detected early.
     *
     * Always use readOnly = true for query-only methods.
     */
    @Transactional(readOnly = true)
    public List<EmployeeEntity> readOnlyExample() {
        return employeeRepository.findAll();
    }

    // ── REQUIRES_NEW practical demo ──────────────────────────────────────────

    /**
     * Demonstrates why REQUIRES_NEW is useful for audit logging.
     * The main operation (salary raise) can fail and roll back,
     * but the audit entry still gets saved in its own transaction.
     */
    @Transactional
    public void giveRaiseWithAudit(Integer deptId, BigDecimal multiplier) {
        int updated = employeeRepository.updateSalaryByDepartment(deptId, multiplier);
        // auditLogService.log() runs in REQUIRES_NEW → commits even if we throw below
        auditLogService.log("Salary updated for " + updated + " employees in dept " + deptId);
        // Simulate a failure after the raise — audit log still committed independently
        // throw new RuntimeException("Simulated failure"); // uncommenting shows REQUIRES_NEW
    }
}
