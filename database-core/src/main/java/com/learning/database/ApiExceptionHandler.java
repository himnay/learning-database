package com.learning.database;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Maps client errors to RFC 9457 problem details instead of a generic 500:
 * constraint violations (unique email, unique product code, FK) → 409,
 * lookups of a missing id → 404, invalid arguments → 400.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Unique / FK / NOT NULL violations raised by the database. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail onConstraintViolation(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMostSpecificCause().getMessage());
        problem.setTitle("Constraint violation");
        return problem;
    }

    /** A row looked up by id does not exist ({@code orElseThrow()} in services/controllers). */
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail onNotFound(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Invalid input, e.g. an unknown isolation level or a negative scroll offset. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onInvalidArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
