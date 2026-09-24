package com.learning.database;

import com.learning.database.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** All 13 Flyway migrations against real PostgreSQL, plus the row-locking demos. */
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
    void pessimisticWriteLockUpdatesSalary() {
        assertThat(employees.updateSalaryWithPessimisticLock(2, new BigDecimal("76000.00")).getSalary())
                .isEqualByComparingTo("76000.00");
    }
}
