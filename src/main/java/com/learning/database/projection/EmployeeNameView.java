package com.learning.database.projection;

import org.springframework.beans.factory.annotation.Value;

/**
 * Interface-based Projection — Spring Data creates a proxy at runtime.
 *
 * Pros over Tuple:
 *   - Type-safe: method names match column aliases in the query.
 *   - No casting required.
 *   - @Value allows SpEL expressions to combine fields.
 *
 * Used in EmployeeRepository.findEmployeeNames().
 */
public interface EmployeeNameView {

    String getFirstName();

    String getLastName();

    // SpEL expression: concatenates two fields into a virtual getter
    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullName();
}
