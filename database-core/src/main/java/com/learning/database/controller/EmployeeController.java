package com.learning.database.controller;

import com.learning.database.entity.interview.EmployeeEntity;
import com.learning.database.projection.EmployeeNameView;
import com.learning.database.projection.EmployeeSummaryDTO;
import com.learning.database.repository.EmployeeRepository;
import com.learning.database.service.EmployeeService;
import com.learning.database.service.ProductService;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes EmployeeRepository / EmployeeService / ProductService demos:
 * derived queries, JPQL, native SQL, projections (interface / DTO / Tuple),
 * paging, slicing, keyset scrolling, QBE, @Modifying, locking, stored procedures,
 * window functions.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final ProductService productService;

    /** Window<T> flattened for JSON: content + hasNext + cursor for the next request. */
    public record WindowResponse<T>(List<T> content, boolean hasNext, Map<String, Object> nextCursor) {}

    static <T> WindowResponse<T> toResponse(Window<T> window) {
        Map<String, Object> cursor = null;
        if (window.hasNext() && !window.isEmpty()) {
            ScrollPosition next = window.positionAt(window.size() - 1);
            if (next instanceof KeysetScrollPosition keyset) {
                cursor = new LinkedHashMap<>(keyset.getKeys());
            } else if (next instanceof OffsetScrollPosition offset) {
                cursor = Map.of("offset", offset.getOffset());
            }
        }
        return new WindowResponse<>(window.getContent(), window.hasNext(), cursor);
    }

    // ── Derived (method-naming) queries ──────────────────────────────────────

    @GetMapping("/by-dept/{deptId}")
    public List<EmployeeEntity> byDepartment(@PathVariable Integer deptId) {
        return employeeRepository.findByDepartment_DeptId(deptId);
    }

    @GetMapping("/top-earner")
    public EmployeeEntity topEarner() {
        return employeeRepository.findFirstByOrderBySalaryDesc().orElse(null);
    }

    @GetMapping("/top5")
    public List<EmployeeEntity> top5(@RequestParam(defaultValue = "0") BigDecimal minSalary) {
        return employeeRepository.findTop5BySalaryGreaterThanOrderBySalaryDesc(minSalary);
    }

    @GetMapping("/exists")
    public Map<String, Boolean> existsByEmail(@RequestParam String email) {
        return Map.of("exists", employeeRepository.existsByEmail(email));
    }

    // ── JPQL / native / @EntityGraph ─────────────────────────────────────────

    @GetMapping("/salary-range")
    public List<EmployeeEntity> salaryRange(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return employeeRepository.findBySalaryRange(min, max);
    }

    /** JOIN FETCH — department loaded in the same query (no N+1). */
    @GetMapping("/by-dept/{deptId}/with-department")
    public List<EmployeeEntity> byDeptWithDepartment(@PathVariable Integer deptId) {
        return employeeRepository.findByDeptIdWithDepartment(deptId);
    }

    /** @EntityGraph("Employee.withDepartment") on a derived query — same effect, no JPQL. */
    @GetMapping("/search")
    public List<EmployeeEntity> searchByFirstName(@RequestParam String name) {
        return employeeRepository.findByFirstNameContaining(name);
    }

    /** Native SQL query. */
    @GetMapping("/by-last-name/{lastName}")
    public List<EmployeeEntity> byLastName(@PathVariable String lastName) {
        return employeeRepository.findByLastName(lastName);
    }

    // ── Projections ──────────────────────────────────────────────────────────

    /** Interface projection with SpEL fullName. */
    @GetMapping("/projections/names")
    public List<EmployeeNameView> names() {
        return employeeRepository.findEmployeeNames();
    }

    /** Class/DTO projection via JPQL constructor expression. */
    @GetMapping("/projections/summaries")
    public List<EmployeeSummaryDTO> summaries() {
        return employeeRepository.findEmployeeSummaries();
    }

    /** Tuple projection — mapped to alias→value maps for JSON. */
    @GetMapping("/projections/tuples")
    public List<Map<String, Object>> tuples() {
        return employeeRepository.findEmployeeTuples().stream().map(EmployeeController::tupleToMap).toList();
    }

    private static Map<String, Object> tupleToMap(Tuple tuple) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (TupleElement<?> element : tuple.getElements()) {
            map.put(element.getAlias(), tuple.get(element));
        }
        return map;
    }

    // ── Paging / Slicing / Keyset scrolling ──────────────────────────────────

    /** Page — runs data + COUNT queries; @EntityGraph loads departments too. */
    @GetMapping
    public Page<EmployeeEntity> page(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size) {
        return employeeRepository.findAll(PageRequest.of(page, size, Sort.by("salary").descending()));
    }

    /** Slice — no COUNT query; only knows hasNext(). */
    @GetMapping("/slice")
    public Slice<EmployeeEntity> slice(@RequestParam String firstName,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size) {
        return employeeRepository.findByFirstName(firstName, PageRequest.of(page, size));
    }

    /**
     * Keyset (cursor) scrolling — first call without params, then pass back
     * nextCursor.salary / nextCursor.empId from the previous response.
     */
    @GetMapping("/scroll")
    public WindowResponse<EmployeeEntity> scroll(@RequestParam(required = false) BigDecimal lastSalary,
                                                 @RequestParam(required = false) Integer lastEmpId) {
        ScrollPosition position = (lastSalary == null || lastEmpId == null)
                ? ScrollPosition.keyset()
                : ScrollPosition.forward(Map.of("salary", lastSalary, "empId", lastEmpId));
        return toResponse(productService.scrollEmployees(position));
    }

    // ── Query By Example ─────────────────────────────────────────────────────

    @GetMapping("/qbe")
    public List<EmployeeEntity> queryByExample(@RequestParam String firstName) {
        return productService.findEmployeesByExample(firstName);
    }

    // ── @Modifying bulk updates / delete ─────────────────────────────────────

    @PutMapping("/raise")
    public Map<String, Integer> giveRaise(@RequestParam Integer deptId, @RequestParam double percentage) {
        return Map.of("updated", productService.giveRaiseByDepartment(deptId, percentage));
    }

    @PutMapping("/{id}/email")
    public Map<String, Integer> updateEmail(@PathVariable Integer id, @RequestParam String email) {
        return Map.of("updated", employeeRepository.updateEmail(id, email));
    }

    @DeleteMapping("/{id}/hard")
    public Map<String, String> hardDelete(@PathVariable Integer id) {
        employeeRepository.hardDeleteById(id);
        return Map.of("status", "hard-deleted employee " + id);
    }

    // ── Locking ──────────────────────────────────────────────────────────────

    /** PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) held for the service transaction. */
    @PutMapping("/{id}/salary-locked")
    public EmployeeEntity updateSalaryLocked(@PathVariable Integer id, @RequestParam BigDecimal salary) {
        return employeeService.updateSalaryWithPessimisticLock(id, salary);
    }

    /** PESSIMISTIC_READ (SELECT ... FOR SHARE). */
    @GetMapping("/{id}/shared-lock")
    public EmployeeEntity readSharedLock(@PathVariable Integer id) {
        return employeeService.readWithSharedLock(id);
    }

    /** OPTIMISTIC — version check at commit, no row lock. */
    @GetMapping("/by-email")
    public EmployeeEntity byEmailOptimistic(@RequestParam String email) {
        return employeeService.findByEmailOptimistic(email).orElse(null);
    }

    // ── Stored procedures / window functions ─────────────────────────────────

    /** @Procedure → @NamedStoredProcedureQuery("Employee.getTotalCount") — needs a tx (service). */
    @GetMapping("/procedures/total-count")
    public Map<String, Integer> totalCount() {
        return Map.of("total", employeeService.getTotalEmployeeCount());
    }

    /** StoredProcedureQuery via EntityManager — IN + multiple OUT params. */
    @GetMapping("/procedures/dept-stats/{deptId}")
    public Map<String, String> deptStats(@PathVariable Integer deptId) {
        return Map.of("stats", productService.getDeptSalaryStats(deptId));
    }

    /** Native window functions: ROW_NUMBER, RANK, DENSE_RANK, NTILE, LAG, LEAD. */
    @GetMapping("/window-functions")
    public List<Map<String, Object>> windowFunctions() {
        String[] cols = {"firstName", "salary", "rowNumber", "rank", "denseRank", "quartile", "prevEmp", "nextEmp"};
        return employeeRepository.findWithWindowFunctions().stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (int i = 0; i < cols.length; i++) {
                        map.put(cols[i], row[i]);
                    }
                    return map;
                })
                .toList();
    }
}
