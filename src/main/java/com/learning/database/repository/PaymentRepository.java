package com.learning.database.repository;

import com.learning.database.entity.inheritance.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    // Polymorphic query: Hibernate performs JOIN between payment + child tables
    List<PaymentEntity> findByAmountGreaterThan(BigDecimal amount);
}
