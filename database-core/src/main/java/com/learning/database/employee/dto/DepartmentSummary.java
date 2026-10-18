package com.learning.database.employee.dto;

/** Summary DTO — employees not fetched by findByDeptNameIgnoreCase, so don't serialize the entity. */
public record DepartmentSummary(Integer deptId, String deptName) {}
