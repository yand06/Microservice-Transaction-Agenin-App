package com.jdt16.agenin.transaction.dto.entity;

import com.jdt16.agenin.transaction.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_PRODUCT_NAME, nullable = false)
    private String transactionEntityDTOProductName;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_PRODUCT_PRICE, nullable = false)
    private BigDecimal transactionEntityDTOProductPrice;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_DATE, nullable = false)
    private LocalDateTime transactionEntityDTODate;

    @Column(name = ColumnNameEntityUtility.COLUMN_TRANSACTION_STATUS, nullable = false)
    private String transactionEntityDTOStatus;
}
