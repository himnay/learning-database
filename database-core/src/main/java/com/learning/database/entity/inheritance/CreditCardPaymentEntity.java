package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JOINED child: only card-specific columns live in `credit_card_payment`.
 * Hibernate joins `payment` + `credit_card_payment` on the shared PK.
 */
@Entity
@Table(name = "credit_card_payment")
@DiscriminatorValue("CreditCard")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class CreditCardPaymentEntity extends PaymentEntity {

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_holder", nullable = false)
    private String cardHolder;
}
