package com.learning.database.inheritance.repository;

import com.learning.database.inheritance.entity.PaymentEntityInheritanceType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntityInheritanceType, Long> {

    /**
     * Finds all payments with an amount strictly greater than the given value.
     * Derived query executed against the {@code PaymentEntityInheritanceType} hierarchy,
     * which is mapped with the JOINED inheritance strategy. Hibernate resolves this
     * polymorphic query by performing a JOIN between the parent payment table and
     * each child (subclass) table.
     *
     * @param amount the exclusive lower bound for the payment amount
     * @return the list of payments whose amount is greater than {@code amount}
     */
    List<PaymentEntityInheritanceType> findByAmountGreaterThan(BigDecimal amount);
}
