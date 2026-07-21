package com.learning.database.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Enables JPA auditing (@CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy).
 * AuditorAware provides the "current user" value for @CreatedBy / @LastModifiedBy.
 * In a real app, this would read from the SecurityContext (e.g. JWT principal).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfig {

    /** Defines the auditor aware bean. */
    @Bean
    public AuditorAware<String> auditorAware() {
        // Replace with SecurityContextHolder.getContext().getAuthentication().getName()
        // in a production application that has Spring Security.
        return () -> Optional.of("system");
    }
}
