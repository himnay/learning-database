package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_transfer_payment")
@DiscriminatorValue("BankTransfer")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class BankTransferPaymentEntity extends PaymentEntity {

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;
}
