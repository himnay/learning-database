package com.learning.database.employee.controller;

import com.learning.database.employee.dto.DepartmentSummary;
import com.learning.database.employee.entity.DepartmentEntity;
import com.learning.database.employee.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Department side of the bidirectional @OneToMany — every endpoint uses JOIN FETCH
 * so the employees collection is initialized before Jackson serializes it
 * (open-in-view=false, so lazy access after the tx would throw).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public List<DepartmentEntity> allWithEmployees() {
        return departmentRepository.findAllWithEmployees();
    }

    @GetMapping("/{deptId}")
    public DepartmentEntity byIdWithEmployees(@PathVariable Integer deptId) {
        return departmentRepository.findByIdWithEmployees(deptId).orElse(null);
    }

    @GetMapping("/by-name")
    public DepartmentSummary byName(@RequestParam String name) {
        return departmentRepository.findByDeptNameIgnoreCase(name)
                .map(d -> new DepartmentSummary(d.getDeptId(), d.getDeptName()))
                .orElse(null);
    }
}
