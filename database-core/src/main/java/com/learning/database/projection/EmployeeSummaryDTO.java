package com.learning.database.projection;

import java.math.BigDecimal;

/**
 * Class-based (DTO) Projection — used with JPQL constructor expression.
 *
 * Query: SELECT new com.learning.database.projection.EmployeeSummaryDTO(e.firstName, e.lastName, e.salary)
 *
 * Pros over interface projection:
 *   - Easier to serialize, pass across layers, and unit-test.
 *   - Works with any JPQL/native query.
 *
 * Cons: query must exactly match the constructor signature.
 */
public record EmployeeSummaryDTO(String firstName, String lastName, BigDecimal salary) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
