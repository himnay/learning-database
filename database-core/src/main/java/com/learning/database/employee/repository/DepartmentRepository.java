package com.learning.database.employee.repository;

import com.learning.database.employee.entity.DepartmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {

    /**
     * Finds a department by its name, ignoring case.
     *
     * @param deptName the department name to search for, case-insensitive
     * @return an {@link Optional} containing the matching department, or empty if none found
     */
    Optional<DepartmentEntity> findByDeptNameIgnoreCase(String deptName);

    /**
     * Finds a department by its id, eagerly fetching its associated employees
     * in a single query via {@code LEFT JOIN FETCH}, avoiding the N+1 select problem.
     *
     * @param deptId the id of the department to fetch
     * @return an {@link Optional} containing the department with its employees, or empty if none found
     */
    @Query("SELECT d FROM DepartmentEntity d LEFT JOIN FETCH d.employees WHERE d.deptId = :deptId")
    Optional<DepartmentEntity> findByIdWithEmployees(Integer deptId);

    /**
     * Retrieves all departments, eagerly fetching their associated employees
     * in a single query via {@code LEFT JOIN FETCH}, avoiding the N+1 select problem.
     *
     * @return the list of all departments with their employees
     */
    @Query("SELECT d FROM DepartmentEntity d LEFT JOIN FETCH d.employees")
    List<DepartmentEntity> findAllWithEmployees();
}
