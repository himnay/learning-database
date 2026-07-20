package com.learning.database.controller;

import com.learning.database.service.TransactionDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Transactional attribute demos (TransactionDemoService).
 * Endpoints report what happened rather than returning entities — the point is the
 * transactional behavior, visible in the SQL/transaction logs.
 *
 * Note: these calls go through the Spring proxy (controller → service), so the
 * annotations actually apply — a self-call inside the service would bypass them.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionDemoService transactionDemoService;

    // ── Propagation ──────────────────────────────────────────────────────────

    @GetMapping("/propagation/{type}")
    public Map<String, String> propagation(@PathVariable String type) {
        try {
            switch (type) {
                case "required"      -> transactionDemoService.requiredExample();
                case "requires-new"  -> transactionDemoService.requiresNewExample("demo action");
                case "mandatory"     -> transactionDemoService.mandatoryExample();
                case "supports"      -> transactionDemoService.supportsExample();
                case "not-supported" -> transactionDemoService.notSupportedExample();
                case "never"         -> transactionDemoService.neverExample();
                case "nested"        -> transactionDemoService.nestedExample();
                default -> {
                    return Map.of("error", "unknown type: " + type
                            + " (use required|requires-new|mandatory|supports|not-supported|never|nested)");
                }
            }
            return Map.of("propagation", type, "result", "completed");
        } catch (Exception e) {
            // MANDATORY without an existing tx lands here — that failure IS the lesson
            return Map.of("propagation", type, "result", "threw " + e.getClass().getSimpleName(),
                    "message", String.valueOf(e.getMessage()));
        }
    }

    // ── Isolation ────────────────────────────────────────────────────────────

    @GetMapping("/isolation/{level}")
    public Map<String, Object> isolation(@PathVariable String level) {
        int count = switch (level) {
            case "read-uncommitted" -> transactionDemoService.readUncommittedExample().size();
            case "read-committed"   -> transactionDemoService.readCommittedExample().size();
            case "repeatable-read"  -> transactionDemoService.repeatableReadExample().size();
            case "serializable"     -> { transactionDemoService.serializableExample(); yield -1; }
            default -> throw new IllegalArgumentException(
                    "use read-uncommitted|read-committed|repeatable-read|serializable");
        };
        return Map.of("isolation", level, "employeesRead", count);
    }

    // ── rollbackFor / noRollbackFor ──────────────────────────────────────────

    /** IllegalArgumentException thrown for a missing id — but the tx still commits. */
    @PostMapping("/no-rollback/{empId}")
    public Map<String, String> noRollback(@PathVariable Integer empId) {
        try {
            transactionDemoService.noRollbackForExample(empId);
            return Map.of("result", "employee exists — no exception thrown");
        } catch (IllegalArgumentException e) {
            return Map.of("result", "exception thrown but transaction COMMITTED (noRollbackFor)",
                    "message", e.getMessage());
        }
    }

    // ── timeout / readOnly ───────────────────────────────────────────────────

    @GetMapping("/timeout")
    public Map<String, Object> timeout() {
        return Map.of("employeesRead", transactionDemoService.timeoutExample().size(),
                "note", "tx rolls back if it exceeds 3s");
    }

    @GetMapping("/read-only")
    public Map<String, Object> readOnly() {
        return Map.of("employeesRead", transactionDemoService.readOnlyExample().size(),
                "note", "dirty checking skipped — faster reads");
    }

    // ── REQUIRES_NEW practical demo ──────────────────────────────────────────

    /** Salary raise + audit log in REQUIRES_NEW — audit commits even if the raise rolls back. */
    @PostMapping("/raise-with-audit")
    public Map<String, String> raiseWithAudit(@RequestParam Integer deptId,
                                              @RequestParam BigDecimal multiplier) {
        transactionDemoService.giveRaiseWithAudit(deptId, multiplier);
        return Map.of("result", "raise applied; audit logged in an independent REQUIRES_NEW tx");
    }
}
