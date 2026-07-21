package com.learning.database.jdbc.controller;

import com.learning.database.jdbc.service.JdbcDemoService;

import com.learning.database.jdbc.service.JdbcDemoService.EmployeeRow;
import com.learning.database.jdbc.service.JdbcDemoService.NewEmployee;
import com.learning.database.jdbc.service.JdbcDemoService.ProductUpsertRow;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC callback styles + batch operations (JdbcDemoService):
 * RowMapper, ResultSetExtractor, RowCallbackHandler, NamedParameterJdbcTemplate,
 * batchUpdate (insert / update / upsert).
 */
@RestController
@RequestMapping("/api/jdbc")
@RequiredArgsConstructor
public class JdbcController {

    private final JdbcDemoService jdbcDemoService;

    /** RowMapper — one typed object per row. */
    @GetMapping("/employees")
    public List<EmployeeRow> allEmployees() {
        return jdbcDemoService.getAllEmployees();
    }

    /** ResultSetExtractor — whole ResultSet folded into one object (dept → names). */
    @GetMapping("/employees/by-department")
    public Map<String, List<String>> byDepartment() {
        return jdbcDemoService.getEmployeesByDepartment();
    }

    /** RowCallbackHandler — rows streamed to the app log, nothing held in memory. */
    @GetMapping("/employees/high-earners")
    public Map<String, Object> highEarners(@RequestParam(defaultValue = "50000") double minSalary) {
        int rows = jdbcDemoService.printHighEarners(minSalary);
        return Map.of("rowsStreamed", rows, "note", "rows were streamed to the application log");
    }

    /** NamedParameterJdbcTemplate — :deptId / :minSalary named params. */
    @GetMapping("/employees/search")
    public List<EmployeeRow> search(@RequestParam int deptId, @RequestParam double minSalary) {
        return jdbcDemoService.findByDeptAndMinSalary(deptId, minSalary);
    }

    @PutMapping("/employees/{id}/email")
    public Map<String, Integer> updateEmail(@PathVariable int id, @RequestParam String email) {
        return Map.of("updated", jdbcDemoService.updateEmailById(id, email));
    }

    /** batchUpdate — bulk INSERT in chunks of 50, one DB round-trip per chunk. */
    @PostMapping("/employees/batch")
    public Map<String, Object> batchInsert(@RequestBody List<NewEmployee> employees) {
        int[][] result = jdbcDemoService.batchInsertEmployees(employees);
        int total = Arrays.stream(result).flatMapToInt(Arrays::stream).sum();
        return Map.of("inserted", total, "chunks", result.length);
    }

    /** batchUpdate via named params — body: { "1": 75000, "2": 82000 }. */
    @PutMapping("/employees/salaries")
    public Map<String, Object> batchUpdateSalaries(@RequestBody Map<Integer, Double> empIdToSalary) {
        int[] result = jdbcDemoService.batchUpdateSalaries(empIdToSalary);
        return Map.of("updated", Arrays.stream(result).sum());
    }

    /** Bulk UPSERT (ON CONFLICT) — run twice with changed prices to see updates instead of inserts. */
    @PostMapping("/products/batch-upsert")
    public Map<String, Object> batchUpsertProducts(@RequestBody List<ProductUpsertRow> rows) {
        int[] result = jdbcDemoService.batchUpsertProducts(rows);
        return Map.of("affected", Arrays.stream(result).sum());
    }
}
