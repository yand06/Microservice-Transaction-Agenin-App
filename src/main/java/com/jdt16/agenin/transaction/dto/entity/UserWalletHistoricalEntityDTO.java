package com.jdt16.agenin.transaction.dto.entity;

import com.jdt16.agenin.transaction.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = TableNameEntityUtility.TABLE_USER_WALLET_HISTORICAL)
public class UserWalletHistoricalEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_WALLET_HISTORICAL_ID, nullable = false, updatable = false)
    private UUID userWalletHistoricalEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_WALLET_HISTORICAL_AMOUNT, nullable = false, updatable = false)
    private BigDecimal userWalletHistoricalEntityDTOAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_WALLET_HISTORICAL_CREATED_DATE, nullable = false)
    private LocalDateTime userWalletHistoricalEntityDTOCreatedDate;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_WALLET_HISTORICAL_USER_WALLET_ID, nullable = false)
    private UUID userWalletHistoricalEntityDTOUserWalletId;
}
