package com.learning.database.jdbc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Demonstrates all Spring JDBC callback styles + batch operations:
 *
 * RowMapper            — map each row to a typed object; get back List<T>
 * ResultSetExtractor   — control the full ResultSet; return any single object
 * RowCallbackHandler   — process each row with no return (streaming/reporting)
 * NamedParameterJdbcTemplate — named :param placeholders instead of positional ?
 * batchUpdate          — efficient bulk INSERT/UPDATE
 */
@Service
@RequiredArgsConstructor
public class JdbcDemoService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbc;

    // ── RowMapper ─────────────────────────────────────────────────────────────
    // Use when: each row maps to one object, you want a List<T> back.

    public record EmployeeRow(int id, String firstName, String lastName, double salary) {}

    public List<EmployeeRow> getAllEmployees() {
        RowMapper<EmployeeRow> mapper = (rs, rowNum) -> new EmployeeRow(
                rs.getInt("emp_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDouble("salary")
        );
        return jdbcTemplate.query("SELECT emp_id, first_name, last_name, salary FROM employees", mapper);
    }

    // ── ResultSetExtractor ────────────────────────────────────────────────────
    // Use when: you need to build a complex single object from multiple rows.

    public Map<String, List<String>> getEmployeesByDepartment() {
        String sql = """
                SELECT d.dept_name, e.first_name || ' ' || e.last_name AS full_name
                FROM employees e
                JOIN departments d ON e.dept_id = d.dept_id
                ORDER BY d.dept_name
                """;

        ResultSetExtractor<Map<String, List<String>>> extractor = rs -> {
            Map<String, List<String>> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.computeIfAbsent(rs.getString("dept_name"), k -> new ArrayList<>())
                      .add(rs.getString("full_name"));
            }
            return result;
        };
        return jdbcTemplate.query(sql, extractor);
    }

    // ── RowCallbackHandler ────────────────────────────────────────────────────
    // Use when: streaming large result sets without holding data in memory.

    /** Streams high earners to the log row-by-row; returns how many rows were processed. */
    public int printHighEarners(double minSalary) {
        String sql = "SELECT first_name, last_name, salary FROM employees WHERE salary > ? ORDER BY salary DESC";
        int[] count = {0};
        RowCallbackHandler handler = rs -> {
            count[0]++;
            System.out.printf("%-12s %-12s  %.2f%n",
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getDouble("salary"));
        };
        jdbcTemplate.query(sql, handler, minSalary);
        return count[0];
    }

    // ── NamedParameterJdbcTemplate ─────────────────────────────────────────
    // Use when: many parameters — named :params are much clearer than positional ?

    /** Finds by dept and min salary. */
    public List<EmployeeRow> findByDeptAndMinSalary(int deptId, double minSalary) {
        String sql = """
                SELECT emp_id, first_name, last_name, salary
                FROM employees
                WHERE dept_id = :deptId AND salary > :minSalary
                ORDER BY salary DESC
                """;
        Map<String, Object> params = Map.of("deptId", deptId, "minSalary", minSalary);
        return namedJdbc.query(sql, params, (rs, rowNum) -> new EmployeeRow(
                rs.getInt("emp_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDouble("salary")
        ));
    }

    /** Updates email by id. */
    public int updateEmailById(int empId, String newEmail) {
        String sql = "UPDATE employees SET email = :email WHERE emp_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", newEmail)
                .addValue("id", empId);
        return namedJdbc.update(sql, params);
    }

    // ── batchUpdate — bulk INSERT ─────────────────────────────────────────────
    // Far more efficient than calling jdbcTemplate.update() in a loop.
    // Sends multiple statements to the DB in a single round-trip.

    public record NewEmployee(String firstName, String lastName, double salary, int deptId) {}

    /** Returns the batch insert employees. */
    @Transactional
    public int[][] batchInsertEmployees(List<NewEmployee> employees) {
        String sql = "INSERT INTO employees (first_name, last_name, salary, dept_id) VALUES (?, ?, ?, ?)";

        // Returns int[][] — outer array: one entry per batch chunk; inner array: rows affected per statement
        return jdbcTemplate.batchUpdate(sql, employees, 50,
                (ps, emp) -> {
                    ps.setString(1, emp.firstName());
                    ps.setString(2, emp.lastName());
                    ps.setDouble(3, emp.salary());
                    ps.setInt(4, emp.deptId());
                });
    }

    // ── batchUpdate — bulk UPDATE via NamedParameterJdbcTemplate ─────────────

    /** Returns the batch update salaries. */
    @Transactional
    public int[] batchUpdateSalaries(Map<Integer, Double> empIdToNewSalary) {
        String sql = "UPDATE employees SET salary = :salary WHERE emp_id = :empId";

        SqlParameterSource[] batch = empIdToNewSalary.entrySet().stream()
                .map(entry -> new MapSqlParameterSource()
                        .addValue("empId", entry.getKey())
                        .addValue("salary", entry.getValue()))
                .toArray(SqlParameterSource[]::new);

        return namedJdbc.batchUpdate(sql, batch);
    }

    // ── batchUpdate — bulk UPSERT (PostgreSQL ON CONFLICT) ───────────────────
    // Bulk upsert: one batched statement per row, each atomically insert-or-update.
    // Requires unique constraint on product.name (V11).

    public record ProductUpsertRow(String name, double price, String category) {}

    /** Bulk-upserts products; returns rows affected per statement. */
    @Transactional
    public int[] batchUpsertProducts(List<ProductUpsertRow> rows) {
        String sql = """
                INSERT INTO product (name, price, category, priority, deleted, version)
                VALUES (:name, :price, :category, 'normal', false, 0)
                ON CONFLICT (name) DO UPDATE SET
                    price    = EXCLUDED.price,
                    category = EXCLUDED.category,
                    version  = product.version + 1
                """;
        SqlParameterSource[] batch = rows.stream()
                .map(r -> new MapSqlParameterSource()
                        .addValue("name", r.name())
                        .addValue("price", r.price())
                        .addValue("category", r.category()))
                .toArray(SqlParameterSource[]::new);
        return namedJdbc.batchUpdate(sql, batch);
    }

    // ── batchUpdate via SqlParameterSourceUtils (entity list → batch) ─────────

    /** Returns the batch update from beans. */
    @Transactional
    public int[] batchUpdateFromBeans(List<EmployeeRow> rows) {
        String sql = "UPDATE employees SET salary = :salary WHERE emp_id = :id";
        // SqlParameterSourceUtils.createBatch maps JavaBean properties to named params
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(rows);
        return namedJdbc.batchUpdate(sql, batch);
    }
}
