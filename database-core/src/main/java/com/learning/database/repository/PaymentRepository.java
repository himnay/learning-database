package com.learning.database.repository;

import com.learning.database.entity.inheritance.PaymentEntityInheritanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntityInheritanceType, Long> {

    // Polymorphic query: Hibernate performs JOIN between payment + child tables
    List<PaymentEntityInheritanceType> findByAmountGreaterThan(BigDecimal amount);
}
