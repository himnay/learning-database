package com.learning.database.controller;

import com.learning.database.controller.EmployeeController.WindowResponse;
import com.learning.database.entity.softdelete.ProductEntity;
import com.learning.database.repository.ProductRepository;
import com.learning.database.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Exposes ProductService / ProductRepository demos:
 * Specification search, soft delete (@SQLDelete + @Filter), paging/slicing/scrolling,
 * @Convert (Priority), @Modifying bulk updates, native upsert, auditing (@CreatedDate
 * etc. visible in every response), optimistic locking (version field).
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    // ── CRUD + auditing demo ─────────────────────────────────────────────────

    /** save() — response shows auditing fields (createdBy/createdDate/version) populated. */
    @PostMapping
    public ProductEntity create(@RequestBody ProductEntity product) {
        return productRepository.save(product);
    }

    @GetMapping("/{id}")
    public ProductEntity byId(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @GetMapping("/exists")
    public Map<String, Boolean> exists(@RequestParam String name) {
        return Map.of("exists", productRepository.existsByNameAndDeletedFalse(name));
    }

    // ── Specification (dynamic search) ───────────────────────────────────────

    /** All params optional — predicates are composed only for the ones supplied. */
    @GetMapping("/search")
    public List<ProductEntity> search(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) BigDecimal minPrice,
                                      @RequestParam(required = false) BigDecimal maxPrice,
                                      @RequestParam(required = false) String keyword) {
        return productService.searchProducts(category, minPrice, maxPrice, keyword);
    }

    // ── Soft delete (@SQLDelete + @Filter) ───────────────────────────────────

    /** deleteById is intercepted by @SQLDelete → UPDATE product SET deleted = true. */
    @DeleteMapping("/{id}")
    public Map<String, String> softDelete(@PathVariable Long id) {
        productService.softDelete(id);
        return Map.of("status", "soft-deleted product " + id);
    }

    /** @Filter enabled with isDeleted=false. */
    @GetMapping("/active")
    public List<ProductEntity> active() {
        return productService.findActiveProducts();
    }

    /** Same filter flipped — proves soft-deleted rows are still in the table. */
    @GetMapping("/deleted")
    public List<ProductEntity> deleted() {
        return productService.findDeletedProducts();
    }

    // ── Paging / Slicing / Scrolling ─────────────────────────────────────────

    @GetMapping("/page")
    public Page<ProductEntity> page(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "3") int size) {
        return productService.getActiveProductsPage(page, size);
    }

    @GetMapping("/slice")
    public Slice<ProductEntity> slice(@RequestParam String category,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "3") int size) {
        return productService.getProductsByCategory(category, page, size);
    }

    /** Offset-based scrolling (ScrollPosition.offset). */
    @GetMapping("/scroll/offset")
    public WindowResponse<ProductEntity> scrollOffset(@RequestParam(defaultValue = "0") int offset) {
        return EmployeeController.toResponse(productService.getProductsWindowOffset(offset, 10));
    }

    /**
     * Keyset scrolling — first call without params, then pass back
     * nextCursor.price / nextCursor.id from the previous response.
     */
    @GetMapping("/scroll/keyset")
    public WindowResponse<ProductEntity> scrollKeyset(@RequestParam(required = false) BigDecimal lastPrice,
                                                      @RequestParam(required = false) Long lastId) {
        ScrollPosition position = (lastPrice == null || lastId == null)
                ? ScrollPosition.keyset()
                : ScrollPosition.forward(Map.of("price", lastPrice, "id", lastId));
        return EmployeeController.toResponse(productService.getProductsWindowKeyset(position));
    }

    // ── @Convert (Priority enum ↔ VARCHAR) ───────────────────────────────────

    @GetMapping("/high-priority")
    public List<ProductEntity> highPriority() {
        return productService.findHighPriorityProducts();
    }

    // ── @Modifying bulk operations ───────────────────────────────────────────

    @PutMapping("/price-adjust")
    public Map<String, Integer> adjustPrice(@RequestParam String category, @RequestParam double factor) {
        return Map.of("updated", productService.increasePriceByCategory(category, factor));
    }

    @DeleteMapping("/category/{category}")
    public Map<String, Integer> softDeleteCategory(@PathVariable String category) {
        return Map.of("softDeleted", productService.softDeleteCategory(category));
    }

    /** Native hard-delete of already-soft-deleted rows. */
    @DeleteMapping("/category/{category}/purge")
    public Map<String, Integer> purge(@PathVariable String category) {
        return Map.of("purged", productRepository.purgeDeletedByCategory(category));
    }

    // ── Upsert (PostgreSQL ON CONFLICT) ──────────────────────────────────────

    /** Insert-or-update in one atomic statement; run twice with different price to see the update path. */
    @PostMapping("/upsert")
    public Map<String, Object> upsert(@RequestParam String name,
                                      @RequestParam BigDecimal price,
                                      @RequestParam(required = false) String category) {
        int affected = productRepository.upsertProduct(name, price, category);
        return Map.of("affected", affected, "name", name);
    }
}
