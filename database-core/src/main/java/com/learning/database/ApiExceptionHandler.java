package com.learning.database;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps constraint violations (unique email, unique product code, FK) to RFC 9457 409s
 * instead of a generic 500 — the database already told us exactly what went wrong.
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
}
