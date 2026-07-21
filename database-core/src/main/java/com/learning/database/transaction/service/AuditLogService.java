package com.learning.database.transaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simulates an audit logging service that must always commit,
 * even if the caller's transaction rolls back.
 *
 * Uses REQUIRES_NEW so every log() call runs in its own transaction.
 * In a real app this would write to an audit_log table.
 */
@Slf4j
@Service
public class AuditLogService {

    /** Logs. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String message) {
        log.info("[AUDIT] {}", message);
        // INSERT INTO audit_log ...
    }
}
