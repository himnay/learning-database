package com.learning.database.softdelete.controller;

import com.learning.database.product.controller.ProductController;
import com.learning.database.softdelete.entity.NoteEntity;
import com.learning.database.softdelete.entity.StockItemEntity;
import com.learning.database.softdelete.repository.NoteRepository;
import com.learning.database.softdelete.repository.StockItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The two "always-on" soft delete styles (contrast with ProductController's @Filter):
 *
 *   stock-items → @SQLDelete + @SQLRestriction("deleted = false")  (Hibernate 6.3+)
 *   notes       → @SoftDelete(columnName = "deleted")              (Hibernate 6.4+)
 *
 * For both: DELETE endpoints run UPDATE ... SET deleted = true, and GET endpoints
 * automatically exclude deleted rows — no filter toggling possible.
 */
@RestController
@RequestMapping("/api/soft-delete")
@RequiredArgsConstructor
public class SoftDeleteController {

    private final StockItemRepository stockItemRepository;
    private final NoteRepository noteRepository;

    // ── @SQLRestriction (StockItemEntity) ────────────────────────────────────

    /** findAll() silently appends WHERE deleted = false. */
    @GetMapping("/stock-items")
    public List<StockItemEntity> stockItems() {
        return stockItemRepository.findAll();
    }

    @GetMapping("/stock-items/in-stock")
    public List<StockItemEntity> inStock(@RequestParam(defaultValue = "0") int minStock) {
        return stockItemRepository.findByStockGreaterThan(minStock);
    }

    @PostMapping("/stock-items")
    public StockItemEntity createStockItem(@RequestBody StockItemEntity item) {
        return stockItemRepository.save(item);
    }

    /** @Modifying bulk UPDATE. */
    @PutMapping("/stock-items/{id}/add-stock")
    public Map<String, Integer> addStock(@PathVariable Long id, @RequestParam int qty) {
        return Map.of("updated", stockItemRepository.addStock(id, qty));
    }

    /** @SQLDelete → UPDATE stock_item SET deleted = true. */
    @DeleteMapping("/stock-items/{id}")
    public Map<String, String> deleteStockItem(@PathVariable Long id) {
        stockItemRepository.deleteById(id);
        return Map.of("status", "soft-deleted stock item " + id + " (row remains, hidden by @SQLRestriction)");
    }

    // ── @SoftDelete (NoteEntity, Hibernate 6.4+) ─────────────────────────────

    @GetMapping("/notes")
    public List<NoteEntity> notes() {
        return noteRepository.findAll();
    }

    @GetMapping("/notes/search")
    public List<NoteEntity> searchNotes(@RequestParam String keyword) {
        return noteRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @PostMapping("/notes")
    public NoteEntity createNote(@RequestBody NoteEntity note) {
        return noteRepository.save(note);
    }

    /** @SoftDelete rewrites this to UPDATE note SET deleted = true — zero config on the repo. */
    @DeleteMapping("/notes/{id}")
    public Map<String, String> deleteNote(@PathVariable Long id) {
        noteRepository.deleteById(id);
        return Map.of("status", "soft-deleted note " + id + " (hidden by @SoftDelete)");
    }
}
