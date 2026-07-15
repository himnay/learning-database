package com.learning.database.repository;

import com.learning.database.entity.interview.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {

    Optional<DepartmentEntity> findByDeptNameIgnoreCase(String deptName);

    // JOIN FETCH avoids N+1: loads department + all employees in a single query
    @Query("SELECT d FROM DepartmentEntity d LEFT JOIN FETCH d.employees WHERE d.deptId = :deptId")
    Optional<DepartmentEntity> findByIdWithEmployees(Integer deptId);

    @Query("SELECT d FROM DepartmentEntity d LEFT JOIN FETCH d.employees")
    List<DepartmentEntity> findAllWithEmployees();
}
