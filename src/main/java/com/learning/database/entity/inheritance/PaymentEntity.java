package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JOINED strategy — parent table `payment` + separate child tables.
 * Child PK = FK to parent PK (Hibernate JOINs them transparently).
 *
 * Pros:  Normalized schema, all columns can be NOT NULL.
 * Cons:  Every query requires a JOIN; slightly slower than SINGLE_TABLE.
 */
@Entity
@Table(name = "payment")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
}
