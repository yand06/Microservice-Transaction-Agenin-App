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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_PRODUCTS)
public class ProductEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_ID, nullable = false, updatable = false)
    private UUID productEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_NAME, nullable = false, updatable = false)
    private String productEntityDTOName;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_CODE, nullable = false, updatable = false)
    private String productEntityDTOCode;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_DESC, nullable = false, updatable = false)
    private String productEntityDTODesc;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_PRICE, nullable = false, updatable = false)
    private String productEntityDTOPrice;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_STATUS, nullable = false, updatable = false)
    private String productEntityDTOStatus;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_CREATED_DATE, nullable = false, updatable = false)
    private LocalDateTime productEntityDTOCreatedAt;

    @Column(name = ColumnNameEntityUtility.COLUMN_PRODUCT_UPDATED_DATE, nullable = false, updatable = false)
    private LocalDateTime productEntityDTOUpdatedAt;
}
