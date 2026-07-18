package com.learning.database.service;

import com.learning.database.entity.converter.Priority;
import com.learning.database.entity.interview.EmployeeEntity;
import com.learning.database.entity.softdelete.ProductEntity;
import com.learning.database.repository.EmployeeRepository;
import com.learning.database.repository.ProductRepository;
import com.learning.database.specification.ProductSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final EntityManager entityManager;

    // ── Soft Delete (@SQLDelete + @Filter) ───────────────────────────────────

    /** Handles soft delete. */
    @Transactional
    public void softDelete(Long id) {
        // @SQLDelete on ProductEntity intercepts this:
        // runs UPDATE product SET deleted=true WHERE id=? AND version=? instead of DELETE
        productRepository.deleteById(id);
    }

    /** Finds active products. */
    @Transactional(readOnly = true)
    public List<ProductEntity> findActiveProducts() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedProductFilter").setParameter("isDeleted", false);
        List<ProductEntity> products = productRepository.findAll();
        session.disableFilter("deletedProductFilter");
        return products;
    }

    /** Finds deleted products. */
    @Transactional(readOnly = true)
    public List<ProductEntity> findDeletedProducts() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedProductFilter").setParameter("isDeleted", true);
        List<ProductEntity> products = productRepository.findAll();
        session.disableFilter("deletedProductFilter");
        return products;
    }

    // ── JPA Specification (dynamic search) ───────────────────────────────────

    /** Searches products. */
    @Transactional(readOnly = true)
    public List<ProductEntity> searchProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        Specification<ProductEntity> spec = ProductSpecification.isNotDeleted()
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                .and(ProductSpecification.nameContains(keyword));
        return productRepository.findAll(spec);
    }

    // ── Query By Example (QBE) ────────────────────────────────────────────────

    /** Finds employees by example. */
    @Transactional(readOnly = true)
    public List<EmployeeEntity> findEmployeesByExample(String firstName) {
        EmployeeEntity probe = new EmployeeEntity();
        probe.setFirstName(firstName);

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnoreNullValues()
                .withIgnorePaths("empId", "salary", "colA", "colB");

        return employeeRepository.findAll(Example.of(probe, matcher));
    }

    // ── Paging, Sorting, Slicing ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductEntity> getActiveProductsPage(int page, int size) {
        // Page triggers COUNT + data queries. Use when total pages matter (UI pagination).
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ProductEntity> getProductsByCategory(String category, int page, int size) {
        // Slice skips COUNT query. Use for "load more" / infinite scroll.
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findByCategory(category, pageable);
    }

    // ── Window / ScrollPosition API (Spring Data JPA 3.1+) ───────────────────

    /**
     * Offset scroll: equivalent to traditional pagination but via the ScrollPosition API.
     * Still uses OFFSET internally — use keyset for better performance on large tables.
     */
    @Transactional(readOnly = true)
    public Window<ProductEntity> getProductsWindowOffset(int offset, int limit) {
        ScrollPosition position = ScrollPosition.offset(offset);
        Sort sort = Sort.by(Sort.Direction.ASC, "price");
        return productRepository.findTop10ByDeletedFalse(position, sort);
    }

    /**
     * Keyset scroll: uses WHERE price > :lastSeenPrice (from the cursor) instead of OFFSET.
     * O(1) regardless of how deep into the result set you scroll.
     *
     * Usage pattern:
     *   Window<ProductEntity> first = getProductsWindowKeyset(ScrollPosition.keyset(), 10);
     *   if (first.hasNext()) {
     *       ScrollPosition next = first.positionAt(first.size() - 1);
     *       Window<ProductEntity> second = getProductsWindowKeyset(next, 10);
     *   }
     */
    @Transactional(readOnly = true)
    public Window<ProductEntity> getProductsWindowKeyset(ScrollPosition position) {
        Sort sort = Sort.by(Sort.Direction.ASC, "price");
        return productRepository.findTop10ByDeletedFalse(position, sort);
    }

    /**
     * Employee keyset scrolling (demonstrates on EmployeeRepository too).
     * Start: ScrollPosition.keyset()
     * Continue: window.positionAt(window.size() - 1)
     */
    @Transactional(readOnly = true)
    public Window<EmployeeEntity> scrollEmployees(ScrollPosition position) {
        return employeeRepository.findTop10By(position, Sort.by(Sort.Direction.DESC, "salary"));
    }

    // ── @Modifying bulk operations ─────────────────────────────────────────────

    /** Returns the give raise by department. */
    @Transactional
    public int giveRaiseByDepartment(Integer deptId, double percentage) {
        BigDecimal multiplier = BigDecimal.valueOf(1 + percentage / 100);
        return employeeRepository.updateSalaryByDepartment(deptId, multiplier);
    }

    /** Returns the increase price by category. */
    @Transactional
    public int increasePriceByCategory(String category, double factor) {
        return productRepository.adjustPriceByCategory(category, BigDecimal.valueOf(factor));
    }

    /** Returns the soft delete category. */
    @Transactional
    public int softDeleteCategory(String category) {
        return productRepository.softDeleteByCategory(category);
    }

    // ── Priority / @Convert demo ─────────────────────────────────────────────

    /** Finds high priority products. */
    @Transactional(readOnly = true)
    public List<ProductEntity> findHighPriorityProducts() {
        // PriorityConverter (autoApply=true) converts Priority.HIGH → "high" in the SQL
        return productRepository.findByPriorityAndDeletedFalse(Priority.HIGH);
    }

    // ── Stored Procedure via EntityManager ────────────────────────────────────

    @Transactional(readOnly = true)
    public String getDeptSalaryStats(Integer deptId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("get_dept_salary_stats");
        query.registerStoredProcedureParameter("p_dept_id", Integer.class,    ParameterMode.IN);
        query.registerStoredProcedureParameter("p_min_sal", BigDecimal.class,  ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_max_sal", BigDecimal.class,  ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_avg_sal", BigDecimal.class,  ParameterMode.OUT);
        query.setParameter("p_dept_id", deptId);
        query.execute();
        return String.format("min=%.2f max=%.2f avg=%.2f",
                query.getOutputParameterValue("p_min_sal"),
                query.getOutputParameterValue("p_max_sal"),
                query.getOutputParameterValue("p_avg_sal"));
    }
}
