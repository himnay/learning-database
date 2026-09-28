package com.learning.database;

import com.learning.database.employee.entity.EmployeeEntity;
import com.learning.database.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** All 14 Flyway migrations against real PostgreSQL, plus the row-locking demos. */
@SpringBootTest
@Testcontainers
class EmployeeLockingIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:19beta3");

    @Autowired
    EmployeeService employees;

    @Test
    void sharedLockReadRunsInAWritableTransaction() {
        // PESSIMISTIC_READ = SELECT ... FOR SHARE, which PostgreSQL rejects inside a read-only transaction
        assertThat(employees.readWithSharedLock(1).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void pessimisticWriteLockUpdatesSalaryAndBumpsTheVersion() {
        EmployeeEntity updated = employees.updateSalaryWithPessimisticLock(2, new BigDecimal("76000.00"));
        assertThat(updated.getSalary()).isEqualByComparingTo("76000.00");
        assertThat(updated.getVersion()).isEqualTo(1L);   // seeded at 0 by V14, +1 by the flushed UPDATE
    }

    @Test
    void optimisticLockReadNeedsTheVersionColumn() {
        // @Lock(OPTIMISTIC) on an entity without @Version fails with
        // "has no version and may not be locked at level OPTIMISTIC" (fixed by V14)
        assertThat(employees.findByEmailOptimistic("alice.murphy@example.com"))
                .hasValueSatisfying(e -> assertThat(e.getVersion()).isNotNull());
    }

    @Test
    void lockingAMissingEmployeeIsNotFound() {
        // NoSuchElementException -> 404 via ApiExceptionHandler (was a 500)
        assertThatThrownBy(() -> employees.readWithSharedLock(9_999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Employee not found: 9999");
    }
}
