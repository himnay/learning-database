package com.learning.database.entity.inheritance.entites;

import com.learning.database.entity.inheritance.PaymentEntityInheritanceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("BankTransfer")
@Table(name = "bank_transfer_payment")
public class BankTransferPaymentEntity extends PaymentEntityInheritanceType {

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;
}
