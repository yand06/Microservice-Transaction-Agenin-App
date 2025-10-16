package com.jdt16.agenin.transaction.dto.entity;

import com.jdt16.agenin.transaction.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_TRANSACTION_OPEN_BANK_ACCOUNT)
public class TransactionOpenBankAccountEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_ID, nullable = false, updatable = false)
    private UUID transactionOpenBankAccountEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_TRANSACTION_ID, nullable = false, updatable = false)
    private UUID transactionOpenBankAccountEntityDTOTransactionId;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_NAME, nullable = false, updatable = false)
    private String transactionOpenBankAccountEntityDTOCustomerName;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_IDENTITY_NUMBER, nullable = false, updatable = false)
    private String transactionOpenBankAccountEntityDTOCustomerIdentityNumber;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_PHONE_NUMBER, nullable = false, updatable = false)
    private String transactionOpenBankAccountEntityDTOCustomerPhoneNumber;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_EMAIL, nullable = false, updatable = false)
    private String transactionOpenBankAccountEntityDTOCustomerEmail;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_ADDRESS, nullable = false, updatable = false)
    private String transactionOpenBankAccountEntityDTOCustomerAddress;
}
