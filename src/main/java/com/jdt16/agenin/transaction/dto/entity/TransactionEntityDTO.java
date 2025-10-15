package com.jdt16.agenin.transaction.dto.entity;

import com.jdt16.agenin.transaction.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_TRANSACTION)
public class TransactionEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_ID, updatable = false, nullable = false)
    private UUID transactionEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_CODE, nullable = false, length = 255)
    private String transactionEntityDTOCode;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_USER_ID, nullable = false)
    private UUID transactionEntityDTOUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_PRODUCT_ID, nullable = false)
    private UUID transactionEntityDTOProductId;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_QUANTITY, nullable = false)
    private BigDecimal transactionEntityDTOQuantity;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_TOTAL_AMOUNT, nullable = false)
    private BigDecimal transactionEntityDTOTotalAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_DATE, nullable = false)
    private LocalDateTime transactionEntityDTODate;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_STATUS, nullable = false)
    private String transactionEntityDTOStatus;
}
