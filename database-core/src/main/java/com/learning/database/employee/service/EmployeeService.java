package com.learning.database.employee.service;

import com.learning.database.employee.entity.EmployeeEntity;
import com.learning.database.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Wraps the @Lock repository methods in transactions — a pessimistic lock is only
 * held for the duration of the surrounding transaction, so calling the repository
 * directly from a controller (no tx) would acquire and instantly release the lock.
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * PESSIMISTIC_WRITE demo: SELECT ... FOR UPDATE locks the row, then the salary
     * update is flushed on commit. A concurrent call for the same id blocks until
     * this transaction ends — try it with two parallel requests.
     */
    @Transactional
    public EmployeeEntity updateSalaryWithPessimisticLock(Integer empId, BigDecimal newSalary) {
        EmployeeEntity employee = employeeRepository.findByIdForUpdate(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));
        employee.setSalary(newSalary);
        return employee; // dirty checking issues the UPDATE at commit, while the row is still locked
    }

    /** PESSIMISTIC_READ demo: SELECT ... FOR SHARE — readers share, writers block. */
    @Transactional(readOnly = true)
    public EmployeeEntity readWithSharedLock(Integer empId) {
        return employeeRepository.findByIdForShare(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));
    }

    /** OPTIMISTIC lock demo: version checked at commit; no DB row lock taken. */
    @Transactional(readOnly = true)
    public Optional<EmployeeEntity> findByEmailOptimistic(String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * @Procedure needs a surrounding transaction to keep the connection open while
     * the result is consumed — calling the repository straight from a controller fails.
     */
    @Transactional(readOnly = true)
    public Integer getTotalEmployeeCount() {
        return employeeRepository.getTotalEmployeeCount();
    }
}
